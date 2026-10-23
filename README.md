# BaseESP (Fabric, Minecraft 1.21.4)

A client-side scouting overlay for nearby visible blocks and entities. The default scan area is **20×20 blocks horizontally**, centered on the player, and scans 4 blocks above/below the player's Y level.

## Features
- Green wireframe outlines around detected chests, trapped chests, barrels, ender chests, shulker boxes, hoppers, dispensers, droppers, and furnaces/smokers.
- Blue wireframe around the 20×20 scan area.
- HUD counts for storage, beds, utility/work blocks, doors, signs, villagers, mobs, and animals.
- A heuristic 0–100 base-likelihood estimate. It is a guess based on visible structures, not proof that a base exists.
- Press **H** to open settings and toggle categories, HUD, base rating, known contents, and scan size. Settings save to `config/baseesp.json`.
- Attempts to display inventory contents only when the client has the inventory data.

## Important multiplayer limitation
Minecraft servers normally do not send the contents of an unopened chest or shulker inventory to the client. This mod does **not** bypass server protections or magically reveal hidden inventory data. Container contents may only be displayed when the server/client has made them available (for example, while the container is open or after data is otherwise legitimately synchronized). The block outline and scan remain useful without contents.

The base rating is a local heuristic and may be inaccurate. The mod only analyzes blocks/entities the client can currently see/load; it does not search unloaded chunks or hidden server data.

## Build
Requires Java 21. From the project directory run:

- Windows: `gradlew.bat build`
- macOS/Linux: `./gradlew build`

The resulting JAR is in `build/libs/`. If Gradle wrapper files are not present, run `gradle wrapper` once using Gradle 8.10.x, then run the commands above. GitHub Actions is configured to build the project automatically when pushed.

## GitHub
Create a repository and upload the contents of this folder (not the ZIP file itself). The Actions workflow will build the mod and upload a downloadable JAR artifact.
