# Auva Client 0.7.0-dev — source work in progress

This is an incremental source revision based on 0.6.1. **Not compiled or tested in Minecraft.**

## Modified source
- HUD editor measures selection boxes using per-module content-aware bounds instead of fixed 125px rectangles, with better dragging, resizing, snapping, and per-widget temporary locking.
- Freelook mouse handling restores head/body yaw after independent camera input; camera orbit now ray-clips against nearby blocks to reduce third-person wall clipping. This is a partial fix, **not a verified full Freelook rebuild**.
- The main menu is replaced with an original responsive, magenta/purple glass navigation screen, custom hover fades and entrance slide. The selected reference's cinematic panorama, transparent logo integration and animated close are **not implemented yet**.
- Music and Smooth Zoom from 0.6.1 are preserved.

## Pending
- Crystal Optimizer and Anchor Optimizer remain explicitly unavailable; no fake toggles or claims of faster server-side actions.
- Full Freelook runtime verification, main menu art assets, animated close transitions, scoreboard and other discovered bugs.

## Build
Run GitHub Actions using the existing workflow. Gradle isn't installed in this working environment; no compilation has been completed for this revision. If compiling fails, share the first compiler error and the failing file/line.
