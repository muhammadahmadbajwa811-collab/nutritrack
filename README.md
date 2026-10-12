# NutriTrack

A multi-page calorie, steps and water tracker built with plain HTML, CSS and vanilla JavaScript (no frameworks).

## Run it
Open `index.html` in a browser. No build step or server needed.

**Quick demo:** on the Login page press "Use demo account" to load a week of sample data.
(Demo login: demo@nutritrack.app / demo1234)

## Folder structure
```
index.html          Landing page
pages/              12 inner pages (about, services, contact, faq, login, register,
                    dashboard, foods, food-details, diary, activity, profile)
css/style.css       Design tokens (light + dark), layout, components, responsive rules
js/data.js          Food database (70 foods), meals, exercise types
js/storage.js       localStorage wrapper, accounts, per-user data, date helpers
js/calc.js          BMR, TDEE, macros, BMI, steps and exercise calculations
js/app.js           Shared UI: theme, navbar, toast, modal, validation, accordion, tabs
js/<page>.js        One script per page
assets/             Favicon
```

## localStorage keys
`nt_users`, `nt_session`, `nt_theme`, `nt_messages`, `nt_data_<email>`

## Notes
- Passwords are lightly hashed in the browser for demonstration only.
- Nutrition values are approximate averages per serving.
- Live step tracking uses the phone motion sensor and is a bonus; manual entry is the reliable method.
