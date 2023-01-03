(WIP) Track sun[burn] exposure and vitamin D based on your location.

Steps along the way:

Minimum Viable Product (for me only) - Basic MVVM + Fragments + Data Binding + Live Data + Koin DI -> 04051bb
https://bitbucket.org/awood82/sunscreen/commits/04051bbbd08c105e58e3903c11908a95d712f661

Bound Service
Tag -> fe591f6 (background_tracking_works, foreground_service_with_binder)
https://bitbucket.org/awood82/sunscreen/commits/fe591f6a8ed27a8cc9cf557e88f4ea679871c191

Modularized architecture (skeleton, :core:etc)
Tag -> d327710 (modularized_architecture)
https://bitbucket.org/awood82/sunscreen/commits/d327710c3008e4f65c46bdb5b5e6f19e9fb115fd

Switching to Flows and Compose, removing Fragments
Tag -> 6220f87 (compose_ui)
https://bitbucket.org/awood82/sunscreen/commits/9e1c63cd75ace148028a1c629401f7cdd68dd92d

Offline-first repository
Tag-> 8878f4c (offline_first_repository)
https://bitbucket.org/awood82/sunscreen/commits/8878f4c5ba53531fedc15cf096346788edfc8955

Service using Repositories to communicate instead -> TODO