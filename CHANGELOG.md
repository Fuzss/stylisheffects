# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v1.2.0-1.16.5] - 2021-12-23
### Added
- Added fallback behavior for effect renderers in case not enough screen space is available
- Added screen border distance option
### Changed
- Hud renderer no longer shows when effect renderer is rendered on a screen (like 1.18)
- Separated screen space options for x- and y-axis
### Fixed
- Fixed effect widgets rendering in front of item tooltips

## [v1.1.1-1.16.5] - 2021-12-17
### Added
- Added a config option for how the vanilla `hideParticles` flag should be respected

## [v1.1.0-1.16.5] - 2021-12-07
### Added
- Added two new config options for excluding a container screen from showing active status effects
### Changed
- Changed internal mechanics for how vanilla effect rendering is prevented allowing for better mod compatibility
### Fixed
- Fixed compatibility with Curios API and Cosmetic Armor Reworked mods

## [v1.0.1-1.16.5] - 2021-11-19
### Changed
- The vanilla effect renderer now supports multiple columns
- Changed default values for a few config options

## [v1.0.0-1.16.5] - 2021-11-02
- Initial release

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
[Puzzles Lib]: https://www.curseforge.com/minecraft/mc-mods/puzzles-lib