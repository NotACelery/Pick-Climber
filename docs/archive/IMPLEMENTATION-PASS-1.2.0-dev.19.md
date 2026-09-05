# Pick Climber 1.2.0-dev.19 — Core Closeout Pass

## Scope

Código funcional primero; assets finales deliberadamente al final.

## Materialized

- Restore World Defaults usa `ConfirmScreen` con confirmación/cancelación explícita.
- Dye slot vacío muestra White Dye vanilla como ghost tenue; no agrega asset provisional nuevo.
- `MAX_EXPLICIT_BLOCKS` baja a 8192.
- Rule Book serializado tiene límite de 512 KiB.
- Payloads con definición completa usan `ByteBufCodecs.compoundTagCodec` con `NbtAccounter` acotado.
- Import/Save hacen preflight cliente y revalidación servidor; el NBT entrante siempre pasa codec + validator + session checks.
- WAITLIST reordenada: JEI/EMI es la próxima prioridad de código; assets finales después.

## Baseline

`1.2.0-dev.18` fue confirmado build-clean en Windows y sigue siendo el baseline estable hasta aceptar dev.19.
