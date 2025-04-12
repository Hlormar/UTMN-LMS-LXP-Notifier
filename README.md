# UTMN LMS & LXP notifier


Is an android application, developed to notify students of Tyumen university about new tasks and upcoming deadlines on UTMN LMS / LXP, also to provide easy access for weekly task schedule. This application is being developed as a product of the English project assignment by a freshman (aproximately 10.2024 - current time).

The application executes the python script using Chaquopy https://github.com/Hlormar/UTMN-LMS-parser to extract the LMS calendar data.
![lms git](https://github.com/user-attachments/assets/a85000ae-5760-46e9-8711-d4396af351fc)


## Cloning
```bash
git clone --recurse-submodules https://github.com/Hlormar/UTMN-LMS-LXP-Notifier.git
```

# TODO
- [ ] Add notification if the activity time changed
- [ ] Add ability to reload schedule if the error occured because of lose of conncetion
- [ ] Switch from python parser to okhttp
