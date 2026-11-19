Vampire Slayer Helper (Fabric, Minecraft 26.2)

Einbau:
1. Dateien in dein Projekt kopieren (build.gradle, settings.gradle, gradle.properties von NameTagMod uebernehmen,
   nur archives_base_name=vampslayerhelper setzen). Nutzt dein Projekt split source sets, den Ordner
   src/main/java nach src/client/java verschieben (Resources bleiben in src/main/resources).
2. Bauen, Mod in den mods-Ordner.
3. Einmal starten -> config/vampslayerhelper.json wird erzeugt.

Einrichtung:
- Es gibt keine Server-Whitelist mehr; die Mod kann auf jedem Server verwendet werden.
- Tasten in den Steuerungs-Einstellungen unter "Sonstiges" belegen: Toggle + Sound-Debug.
- Sound-Debug einschalten, auf dem Testserver ein Twinclaw ausloesen, die Sound-ID aus dem Chat in
  "twinclawSoundId" eintragen, Debug wieder ausschalten.
- "holyIceSlot": Hotbar-Slot (1-9) von Holy Ice.
