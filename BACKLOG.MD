# BACKLOG:

| Task                              | Priority | Weight | Task Description                                                                                                             |
|-----------------------------------|----------|--------|------------------------------------------------------------------------------------------------------------------------------|
| Header                            | 5        | 4      | Add a header to all pages (Main, Login/Logout/Registration)                                                                  |
| Main page                         | 5        | 14     | Background, content overlay, image, generate button, feed, likes/dislikes                                                    |
| Recommendations page              | 5        | 5      | Guide generation button, navigate to the guide page, align with the main page, fix content, single upload, scroll to top     |
| Guide page                        | 5        | 8      | New component, Markdown processing, owner, likes/dislikes (display and functionality), align with the main page              |
| Questionnaire                     | 5        | 3      | Fix content (e.g., budget and source), align with the main page, direct navigation to recommendations page, preference field |
| Login/Registration window & logic | 5        | 3      | Remove mandatory login, refine the window itself, validations                                                                |
| VPN on the host                   | 5        | 3      | Requests to ChatGPT from the host                                                                                            |
| Loading screen                    | 4        | 5      | Loading screen with approximate waiting time                                                                                 |
| Translation to English            | 4        | 6      | Translate everything into English, add a header to the top, new domain                                                       |
| Contact information               | 3        | 3      | Separate screen with my contact info, add to the header                                                                      |
| Bulk generation tool              | 3        | 2      | Generate queries with conditions, generate N articles on button click                                                        |
| Images not always loading         | 3        | 4?     | Investigate why and potentially save them to the database                                                                    |
| Performance tuning                | 3        | 6?     | Optimize queries and split them into smaller parts                                                                           |
| Mobile portable                   | 3        | 4?     | Investigate and make portable                                                                                                |
| Backend/Frontend errors           | 3        | 3      | Plan a general schema, return backend errors, display correctly on the frontend                                              |
| Better image client               | 3        | 3      | Research and use a better client                                                                                             |
| Refine frontend                   | 3        | 2      | Figure out the intended look and fix it                                                                                      |
| Improve CI/CD                     | 2        | 3      | Deploy with a single button                                                                                                  |
| Extend README for certificates    | 2        | 2      | Review the process again and describe it in the README                                                                       |
| Swagger/OpenAPI                   | 2        | 3      | Use                                                                                                                          |
| Separate database                 | 2        | 5      | Move the database to a separate host, use appropriate tools                                                                  |
| Refine backend                    | 1        | 4      | Formatting, fix all highlighted issues, analyzer                                                                             |
| Add caching                       | 1        | 4      | Memcache, decide where to use and use                                                                                        |
| Monitoring (Prometheus + Grafana) | 1        | 5      | Add monitoring                                                                                                               |
| Kubernetes                        | 1        | 6      | Use                                                                                                                          |
| New features?                     | 1        | 20?    | Guide content management?                                                                                                    |

# PLAN

| Task                                | Weight | Task Description                                                                                                             |
|-------------------------------------|--------|------------------------------------------------------------------------------------------------------------------------------|
| Guide page 1                        | 5/8    | New component, Markdown processing, JUST show content                                                                        |
| Recommendations page 1              | 2/5    | Guide generation button, navigate to the Guide page                                                                          |
| VPN on the host                     | 3/3    | Requests to ChatGPT from the host                                                                                            |
| Login/Registration window & logic 1 | 1/3    | Remove mandatory login, refine the window itself, validations                                                                |
| Main page 1                         | 7/14   | Background, content overlay, image, generate button                                                                          |
| Main page 2                         | 7/14   | Feed, likes/dislikes                                                                                                         |
| Header                              | 4/4    | Add a header to all pages (Main, Login/Logout/Registration)                                                                  |
| Questionnaire                       | 3/3    | Fix content (e.g., budget and source), align with the main page, direct navigation to recommendations page, preference field |
| Recommendations page 2              | 3/5    | Align with the main page, fix content, single upload, scroll to top                                                          |
| Guide page 2                        | 3/8    | Owner, likes/dislikes (display and functionality), align with the main page                                                  |
| Login/Registration window & logic 2 | 2/3    | Refine the window itself, validations                                                                                        |