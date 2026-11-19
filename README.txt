Vampire Slayer Helper (Fabric, Minecraft 26.2)

Erkennung (wie SkyHanni, siehe VampireSlayerFeatures.kt):
- Twinclaws: Armor Stand mit Namen "TWINCLAWS ..." nahe dem Boss (max. 5 Bloecke), hoechstens alle 5 s
- Impel: Title/Subtitle "Impel: CLICK UP 1.8s" (Hud.setTitle / setSubtitle)

Einbau:
1. Dateien in dein Projekt kopieren (build.gradle, settings.gradle, gradle.properties von NameTagMod uebernehmen,
   nur archives_base_name=vampslayerhelper setzen). Nutzt dein Projekt split source sets, den Ordner
   src/main/java nach src/client/java verschieben (Resources bleiben in src/main/resources).
2. Bauen, Mod in den mods-Ordner, einmal starten -> config/vampslayerhelper.json wird erzeugt.

Einrichtung:
- Die Automatik ist auf jedem verbundenen Multiplayer-Server freigegeben, einschliesslich Hypixel.
- Singleplayer bleibt standardmaessig deaktiviert und kann ueber "allowSingleplayer": true aktiviert werden.
- Toggle-Taste in den Steuerungs-Einstellungen unter "Sonstiges" belegen.
- Hotbar-Slots (Standard): holyIceSlot=4, healingMelonSlot=2. Slot 1 (Schwert) und Slot 3 (Kill-Schwert) bedient die Mod nicht.
- Auto-Heilung: ab "healThresholdPercent" (50) HP oder weniger wird auf die Healing Melon gewechselt, rechtsgeklickt und zurueckgewechselt.
  "healOnlyNearBoss": nur wenn ein Boss in der Naehe ist. Twinclaws hat Vorrang vor der Heilung.
- "bossNameContains": Namensteil des Boss-Spielers (Standard "Bloodfiend").
