# World Tier (Fabric, Minecraft 26.2)

Progresi ala Terraria. Kalahkan boss -> bijih baru terbuka, dunia makin sulit.

| Boss | Efek |
|---|---|
| Elder Guardian | Mythril Ore bisa ditambang |
| Wither | Adamantite Ore terbuka + HARDMODE (monster +30% HP) |
| Warden | monster makin kuat (+60%) |
| Ender Dragon | tier tertinggi (+90%) |

- Bijih menjatuhkan ingot langsung. Buat pedang/beliung Mythril & Adamantite di crafting table.
- Mythril ~ diamond, Adamantite ~ lebih kuat dari netherite.
- Perintah `/worldtier` menampilkan progres. Creative mode tidak terkena gerbang bijih.
- Bijih hanya muncul di chunk BARU (buat dunia baru).

## Build
Perlu JDK 25.
- Lokal: install Gradle 9.5.1 lalu `gradle build` -> `build/libs/worldtier-1.0.0.jar`
- Tanpa setup: upload folder ini ke GitHub, tab Actions -> jalankan "build" -> unduh artifact.

## Pasang di Prism Launcher
Instance Minecraft 26.2 + Fabric Loader, lalu taruh `worldtier-1.0.0.jar` dan Fabric API (26.2) di folder `mods`.
