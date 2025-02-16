# BACKLOG:

| Task                                  | Priority | Weight | Task Description                                                                                                             |
|---------------------------------------|----------|--------|------------------------------------------------------------------------------------------------------------------------------|
| ~~Header~~                            | 5        | 4      | Add a header to all pages (Main, Login/Logout/Registration)                                                                  |
| ~~Main page~~                         | 5        | 14     | Background, content overlay, image, generate button, feed, likes/dislikes                                                    |
| ~~Recommendations page~~              | 5        | 5      | Guide generation button, navigate to the guide page, align with the main page, fix content, single upload, scroll to top     |
| ~~Guide page~~                        | 5        | 8      | New component, Markdown processing, owner, likes/dislikes (display and functionality), align with the main page              |
| ~~Questionnaire~~                     | 5        | 3      | Fix content (e.g., budget and source), align with the main page, direct navigation to recommendations page, preference field |
| ~~Login/Registration window & logic~~ | 5        | 3      | Remove mandatory login, refine the window itself, validations                                                                |
| ~~VPN on the host~~                   | 5        | 3      | Requests to ChatGPT from the host                                                                                            |
| ~~Loading screen~~                    | 4        | 5      | Loading screen with approximate waiting time                                                                                 |
| ~~Translation to English~~            | 4        | 6      | Translate everything into English, add a header to the top, new domain                                                       |
| ~~Contact information~~               | 3        | 3      | Separate screen with my contact info, add to the header                                                                      |
| ~~Bulk generation tool~~              | 3        | 2      | Generate queries with conditions, generate N articles on button click                                                        |
| ~~Images not always loading~~         | 3        | 4?     | Investigate why and potentially save them to the database                                                                    |
| ~~Performance tuning~~                | 3        | 6?     | Optimize queries and split them into smaller parts                                                                           |
| ~~Mobile portable~~                   | 3        | 4?     | Investigate and make portable                                                                                                |
| ~~Better image client~~               | 3        | 3      | Research and use a better client                                                                                             |
| ~~Refine frontend~~                   | 3        | 2      | Figure out the intended look and fix it                                                                                      |
| ~~AI GENERATED, BE AWARE~~            | 3        | 3      | Implement caption and logo                                                                                                   |
| ~~Your session is expired~~           | 3        | 3      | Show error for user                                                                                                          |
| ~~Generate image with AI~~            | 3        | 12     | Generate image with ai; create client; implement endpoint for generating images                                              |
| ~~Admin endpoint~~                    | 3        | 7      | Implement endpoint for admin; upload image                                                                                   |
| ~~Authorization and roles~~           | 3        | 3      | Only ADMIN has access to /admin                                                                                              |
| Backend/Frontend errors               | 3        | 3      | Plan a general schema, return backend errors, display correctly on the frontend                                              |
| Improve CI/CD                         | 2        | 3      | Deploy with a single button                                                                                                  |
| Extend README for certificates        | 2        | 2      | Review the process again and describe it in the README                                                                       |
| Swagger/OpenAPI                       | 2        | 3      | Use                                                                                                                          |
| Separate database                     | 2        | 5      | Move the database to a separate host, use appropriate tools                                                                  |
| Refine backend                        | 1        | 4      | Formatting, fix all highlighted issues, analyzer                                                                             |
| Add caching                           | 1        | 4      | Memcache, decide where to use and use                                                                                        |
| Monitoring (Prometheus + Grafana)     | 1        | 5      | Add monitoring                                                                                                               |
| Kubernetes                            | 1        | 6      | Use                                                                                                                          |
| New features?                         | 1        | 20?    | Guide content management?                                                                                                    |
| Contact page                          | 1        | 2      | Page with contacts (telegram, email, linkedin)                                                                               |
| Fetch user's location                 | 3        | 5      | Define location and show                                                                                                     |
| Optimistic locks and retries          | 2        | 5      | Add optimistic locks and retries                                                                                             |
| Improve repo                          | 2        | ?      | https://github.com/pyatkinmv/pognaleey/community                                                                             |
| Fix one-guide-same-image repeating    | 2        | 3      | Multiple same images on one guide page; fix                                                                                  |
| Websockets instead of polling         | 3        | 10     | Polling is ugly; implement websockets                                                                                        |
| Language switch tests                 | 2        | 3      | Implement tests                                                                                                              |
| Guide's number of views               | 2        | 4      | Store and show number of views for guide                                                                                     |
| Refactor Polling                      | 1        | 2      | Refactor pognaleey-front/src/hooks/usePolling.ts                                                                             |

# PLAN 1/13/2025

| Task                                    | Weight | Task Description                                                                                                             |
|-----------------------------------------|--------|------------------------------------------------------------------------------------------------------------------------------|
| ~~Guide page 1~~                        | 5/8    | New component, Markdown processing, JUST show content                                                                        |
| ~~Recommendations page 1~~              | 2/5    | Guide generation button, navigate to the Guide page                                                                          |
| ~~VPN on the host~~                     | 3/3    | Requests to ChatGPT from the host                                                                                            |
| ~~Login/Registration window & logic 1~~ | 1/3    | Remove mandatory login, refine the window itself, validations                                                                |
| ~~Main page 1~~                         | 7/14   | Background, content overlay, image, generate button                                                                          |
| ~~Main page 2~~                         | 7/14   | Feed, likes/dislikes                                                                                                         |
| ~~Logo~~                                | 2/2    | Generate, format and use logo for header                                                                                     |
| ~~Header 1~~                            | 4/6    | Implement login/logout/user                                                                                                  |
| ~~Header 2~~                            | 2/6    | Implement lang & contact; add a header to all pages (Main, Login/Logout/Registration)                                        |
| ~~Questionnaire~~                       | 3/3    | Fix content (e.g., budget and source), align with the main page, direct navigation to recommendations page, preference field |
| ~~Recommendations page 2~~              | 3/5    | Align with the main page, fix content, single upload, scroll to top                                                          |
| ~~Guide page 2~~                        | 3/8    | Owner, likes/dislikes (display and functionality), align with the main page                                                  |
| ~~Login/Registration window & logic 2~~ | 2/3    | Refine the window itself, validations                                                                                        |
