# Snapshot Status — 2026-09-02 — 1.2.0-dev.21

## Propósito

Este snapshot es el handoff acumulativo actual de Pick Climber 1.2.0 y permite continuar sin reconstruir decisiones
ni mezclar archivos legacy de builds previos.

## Code floor físico

```text
Minecraft 1.21.1
NeoForge 21.1.235
Java toolchain 21
Pick Climber 1.2.0-dev.21
network protocol 15
Rule Book portable format 2
mechanical profile format 1
```

Historia relevante:

```text
1.1.0 stable
 -> recovered 1.2.0-dev.8 floor
 -> dev.9 Rule Book / Rules Table / input-only Terminal reconstruction
 -> dev.10 final authoring + JSON v2 + Permanent/Temporary WORLD
 -> dev.11 Temporary PLAYER + per-player sync + rules countdown HUD
 -> dev.12 Rule Dispenser + Temporary Rule Book transport + Rules/Book HUD
 -> dev.13 Structural Geometry Safety + portable Windows integrity gate
 -> dev.14-dev.16 build-unblock campaign + Windows clean build SUCCESS
 -> dev.17 Viewer read-only + Duplicate GUI + Windows clean build SUCCESS
 -> dev.18 portable identity/JSON/migration hardening + Windows clean build SUCCESS
 -> dev.19 Table UX/networking hardening + Windows clean build SUCCESS
 -> dev.20 approved visual assets + dynamic Rule Book cover tint
 -> dev.21 optional JEI/EMI documentation integrations
```

## Baseline build-clean

El usuario confirmó `clean build` SUCCESS en Windows sobre el source root-ready exacto de `1.2.0-dev.20`.
Por lo tanto dev.20 es el baseline estable/build-clean vigente debajo de esta pasada.

`1.2.0-dev.21` todavía requiere su propio build de aceptación.

## Materializado hasta dev.21

La funcionalidad acumulada incluye:

- Climbing Rule Book portable v2;
- Climbing Rules Table para autoría, ahora con orientación horizontal visual;
- Climbing Rules Terminal input-only y orientable en seis direcciones;
- Permanent WORLD, Temporary WORLD y Temporary PLAYER;
- effective-rule precedence PLAYER -> WORLD -> defaults;
- Rule Dispenser Creative-only con Temporary Rule Book transport owner-bound;
- HUD Rules / Book;
- Structural Geometry Safety basada en collision shape concreta;
- Viewer read-only y Duplicate GUI;
- JSON v2 + compat/migración legacy estricta;
- límites de networking 8192 overrides / 512 KiB;
- ocho locales con paridad;
- Rule Book 32x32 en dos capas perfectamente alineadas;
- cover tint dinámico desde `DyeColor` para Rule Book normal y Temporary Rule Book;
- páginas, herrajes, rombo e icono de picota sin tint;
- el emblema usa el `pickaxe_indicator.png` real ya existente en Pick Climber;
- Table, Terminal y Dispenser con texturas aprobadas 64x64;
- Terminal y Dispenser con modelos horizontal/vertical separados.
- JEI/EMI opcionales con relaciones visuales sintéticas y sin hard dependency.

`Persist on Pickup` permanece deliberadamente en 1.3.0.

## Pendiente funcional antes del feature freeze

`docs/WAITLIST.md` sigue siendo la fuente operativa. Fuera de QA/documentación, queda principalmente:

- refinamiento visual del Dispenser UP/DOWN: el seam y la textura dedicada ya existen, pero dev.20 replica todavía el
  front aprobado normal en `climbing_rule_dispenser_front_vertical.png`;
- aceptación de build de dev.21 y cualquier fix de API/render que aparezca.
