# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.3.4-1.19.2] - 2023-05-12
### Changed
- Effect widgets no longer render while the debug menu is being displayed on Forge as they would render on top due to Forge's internal gui implementation

## [v4.3.3-1.19.2] - 2023-03-28
### Fixed
- Fixed [JEI](https://www.curseforge.com/minecraft/mc-mods/jei) integration
- The Stylish Effects inventory display is now disabled by default for all [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism) machines

## [v4.3.2-1.19.2] - 2022-10-16
### Fixed
- Fixed integration for REI

## [v4.3.1-1.19.2] - 2022-10-15
### Fixed
- Fixed visual glitch on main menu with FancyMenu mod on Fabric

## [v4.3.0-1.19.2] - 2022-08-21
- Compiled for Minecraft 1.19.2
- Updated to Puzzles Lib v4.2.0

## [v4.2.0-1.19.1] - 2022-08-01
- Compiled for Minecraft 1.19.1
- Updated to Puzzles Lib v4.1.0

## [v4.1.3-1.19] - 2022-07-27
### Added
- Re-enabled REI support on Forge
### Fixed
- Fixed effect widgets not showing immediately after a new screen is opened

## [v4.1.2-1.19] - 2022-07-25
### Added
- Added a scale option for all widgets

## [v4.1.1-1.19] - 2022-07-23
### Fixed
- Reset color after rendering, so that no other screen elements appear with the wrong color
- Fixed effect tooltips rendering when an item is being carried by the cursor

## [v4.1.0-1.19] - 2022-07-23
### Added
- Added a new api for other mods to allow for compat with various mob effect widget related things
- Added two new widget modes (both adapted from vanilla, but with plenty of customization options)
- Added an option to show a custom effect widget border depending on whether the effect is beneficial or not
### Fixed
- Fixed effect tooltips sometimes rendering behind other screen components
### Removed
- Removed ability to skip rendering overflowing effects, they'll now always stack on top of each other to prevent overflowing

## [v4.0.1-1.19] - 2022-07-22
### Added
- Re-added REI support on Fabric (Forge has to wait for an update to REI)

## [v4.0.0-1.19] - 2022-07-21
- Ported to Minecraft 1.19
- Split into multi-loader project
### Added
- Added JEI support on Fabric
### Removed
- Removed support for REI, will come back in the next version

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
