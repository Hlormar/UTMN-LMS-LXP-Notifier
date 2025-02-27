# UTMN LMS & LXP notifier
Is an android application, developed to notify students of Tyumen university about new tasks and upcoming deadlines on UTMN LMS / LXP, also to provide easy access for weekly task schedule. This application is being developed as a product of the English project assignment by freshmans (aproximately 10.2024 - current time).

The application executes the python script using Chaquopy https://github.com/Hlormar/UTMN-LMS-parser to extract the LMS calendar data.

## Cloning
```bash
git clone --recurse-submodules https://github.com/Hlormar/UTMN-LMS-LXP-Notifier.git
```

# TODO
- [x] Fix the layout scaling (some bottom elements are not showing appropriately on some devices)
- [ ] Improve the design
- [x] Add courses names
- [x] Make switching to schedule tab independent from parsing process
- [x] Render description as html
- [x] Make every activity survive config changes (rotation, theme changing)
- [x] Add loading animation
- [x] Make each note as a clickable card view
- [x] Add credits to settings
- [x] Modify time format
- [x] Provide android 9.0 support
- [x] Add notification for new tasks appearing in calendar
- [x] Add notification about upcoming deadline (timestart)
- [ ] Add customizable amount of auto-checks per day (min is disabled, max is 24, default is 3)
- [ ] RU/EN
- [ ] Make automated checking starts at time defined by user (default 6am)
- [ ] Limit the amount of reloadings per 15 minutes, to reduce the server load

![5asdf](https://github.com/user-attachments/assets/2d320ee2-cfe5-4212-aa1b-530a9fcdd24c) ![3asdf](https://github.com/user-attachments/assets/b0275403-b822-4779-b768-120f641d1671) ![2asdf](https://github.com/user-attachments/assets/15619b2b-af85-4fad-b699-715330bf5601)
