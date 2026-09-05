param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectRoot,
    [Parameter(Mandatory = $true)]
    [string]$EditableSourceRoot
)

Add-Type -AssemblyName System.Drawing

function Clear-ExteriorBlackBackground {
    param([string]$Path)

    $bitmap = [System.Drawing.Bitmap]::FromFile($Path)
    try {
        $width = $bitmap.Width
        $height = $bitmap.Height
        $visited = New-Object 'bool[,]' $width, $height
        $queue = [System.Collections.Generic.Queue[System.Drawing.Point]]::new()
        $cleared = 0

        function Add-If-ExteriorBlack([int]$X, [int]$Y) {
            if ($X -lt 0 -or $Y -lt 0 -or $X -ge $width -or $Y -ge $height -or $visited[$X, $Y]) {
                return
            }
            $pixel = $bitmap.GetPixel($X, $Y)
            if ($pixel.A -ne 0 -and $pixel.R -eq 0 -and $pixel.G -eq 0 -and $pixel.B -eq 0) {
                $visited[$X, $Y] = $true
                $queue.Enqueue([System.Drawing.Point]::new($X, $Y))
            }
        }

        for ($x = 0; $x -lt $width; $x++) {
            Add-If-ExteriorBlack $x 0
            Add-If-ExteriorBlack $x ($height - 1)
        }
        for ($y = 0; $y -lt $height; $y++) {
            Add-If-ExteriorBlack 0 $y
            Add-If-ExteriorBlack ($width - 1) $y
        }
        while ($queue.Count -gt 0) {
            $point = $queue.Dequeue()
            $bitmap.SetPixel($point.X, $point.Y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
            $cleared++
            Add-If-ExteriorBlack ($point.X - 1) $point.Y
            Add-If-ExteriorBlack ($point.X + 1) $point.Y
            Add-If-ExteriorBlack $point.X ($point.Y - 1)
            Add-If-ExteriorBlack $point.X ($point.Y + 1)
        }
        $temporary = "$Path.dev46.png"
        $bitmap.Save($temporary, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally {
        $bitmap.Dispose()
    }
    Move-Item -LiteralPath $temporary -Destination $Path -Force
    return $cleared
}

function Update-EditableModel {
    param([string]$Path)

    $model = Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json
    foreach ($element in $model.elements) {
        if ($element.name -in @('Top Wall', 'Bottom Wall')) {
            $element.faces.PSObject.Properties.Remove('down')
        }
        if ($element.name -eq 'Book') {
            $element.from[1] = [double]$element.from[1] + 0.25
            $element.to[1] = [double]$element.to[1] + 0.25
            $element.rotation.origin[1] = [double]$element.rotation.origin[1] + 0.25
        }
    }
    $model | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $Path -Encoding utf8
}

function Export-RuntimeModel {
    param([string]$SourcePath, [string]$DestinationPath)

    $model = Get-Content -Raw -LiteralPath $SourcePath | ConvertFrom-Json
    $model.PSObject.Properties.Remove('format_version')
    $model.PSObject.Properties.Remove('credit')
    $model.PSObject.Properties.Remove('groups')
    $model.textures = [PSCustomObject][ordered]@{
        '0' = 'pickclimber:block/climbing_rules_table_bookshelf'
        '1' = 'pickclimber:block/climbing_rules_table_oakplank'
        '2' = 'pickclimber:block/climbing_rules_table_book_open'
        '3' = 'pickclimber:block/climbing_rules_table_palette'
        particle = 'pickclimber:block/climbing_rules_table_bookshelf'
    }
    foreach ($element in $model.elements) {
        if ($element.name -in @('Top Wall', 'Bottom Wall')) {
            $element.faces.PSObject.Properties.Remove('down')
        }
        if ($element.name -eq 'Book') {
            $element.from[1] = [double]$element.from[1] + 0.25
            $element.to[1] = [double]$element.to[1] + 0.25
            $element.rotation.origin[1] = [double]$element.rotation.origin[1] + 0.25
        }
        foreach ($face in @($element.faces.PSObject.Properties)) {
            if ($face.Value.texture -eq '#missing') {
                $element.faces.PSObject.Properties.Remove($face.Name)
            }
        }
    }
    $model | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $DestinationPath -Encoding utf8
}

$runtimeTextures = Join-Path $ProjectRoot 'src/main/resources/assets/pickclimber/textures/block'
$runtimeModels = Join-Path $ProjectRoot 'src/main/resources/assets/pickclimber/models/block'
$editableRoot = Join-Path (Split-Path $ProjectRoot -Parent) 'Pick-Climber-1.2.0-dev.46-Blockbench-assets'
Copy-Item -LiteralPath $EditableSourceRoot -Destination $editableRoot -Recurse -Force
$editableFiles = Get-ChildItem -LiteralPath $editableRoot -Filter 'climbing_rules_table*.json'
foreach ($file in $editableFiles) { Update-EditableModel $file.FullName }

Copy-Item -LiteralPath (Join-Path $EditableSourceRoot 'bookshelf.png') -Destination (Join-Path $runtimeTextures 'climbing_rules_table_bookshelf.png') -Force
Copy-Item -LiteralPath (Join-Path $EditableSourceRoot 'oakplank.png') -Destination (Join-Path $runtimeTextures 'climbing_rules_table_oakplank.png') -Force
Copy-Item -LiteralPath (Join-Path $EditableSourceRoot 'Pallet.png') -Destination (Join-Path $runtimeTextures 'climbing_rules_table_palette.png') -Force
Copy-Item -LiteralPath (Join-Path $EditableSourceRoot 'climbing_rules_table_book_open.png') -Destination (Join-Path $runtimeTextures 'climbing_rules_table_book_open.png') -Force
$clearedRuntime = Clear-ExteriorBlackBackground (Join-Path $runtimeTextures 'climbing_rules_table_book_open.png')
$clearedEditable = Clear-ExteriorBlackBackground (Join-Path $editableRoot 'climbing_rules_table_book_open.png')
Export-RuntimeModel (Join-Path $EditableSourceRoot 'climbing_rules_table.json') (Join-Path $runtimeModels 'climbing_rules_table.json')
Export-RuntimeModel (Join-Path $EditableSourceRoot 'climbing_rules_table_with_book.json') (Join-Path $runtimeModels 'climbing_rules_table_with_book.json')
Write-Output "Exterior black pixels made transparent: runtime=$clearedRuntime editable=$clearedEditable"
