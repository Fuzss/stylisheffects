# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

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
