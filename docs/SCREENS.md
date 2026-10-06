# Deen Journey — screen inventory & flows

Source boards: `docs/1.webp` (practical tools), `2.webp` (salah & worship), `3.webp` (knowledge & family),
`4.webp` (Quran), `5.webp` (onboarding & family). `[n.m]` = board n, screen m. `+` = added (missing in boards).

Bottom navigation (5 tabs): **Home · Quran · Learn · Worship · More**. Login is required (Firebase Auth),
so the boards' "Explore as Guest" becomes "I already have an account".

## A · Start & account
| id | screen | origin | notes |
|---|---|---|---|
| A01 | Splash | + | logo, loads prayer data |
| A02 | Welcome & language | [5.1] | EN / اردو / العربية, Create account, Sign in |
| A03 | Sign in | + | email+password, Google, Apple (iOS), forgot link |
| A04 | Create account | + | name, email, password, terms |
| A05 | Forgot password | + | email → reset link |
| A06 | Check your email | + | reset / verification sent |
| A07 | Location & prayer setup | + | permission rationale, manual city, auto method |
| A08 | Notifications permission | + | adhan & reminders rationale |

## B · Family onboarding
| B01 | Family profiles | [5.2] | Child / Parent / Adult / Senior |
| B02 | Age & learning level | [5.3] | 2–5 / 6–9 / 10–12, Beginner–Advanced |
| B03 | Learning goals | [5.4] | 5/10/15 min, focus areas |
| B04 | Add family member | + | name, relation, age group, avatar |
| B05 | You're all set | + | summary → Home |

## C · Home & hubs
| C01 | Home | [5.5] | next prayer countdown, continue Quran, lesson, hadith, good deed |
| C02 | Notifications | + | adhan, lessons, Ramadan |
| C03 | Switch profile (sheet) | + | family members, add |
| C04 | Learn hub | + | roadmap, hadith, akhlaq, seerah, 99 names, progress |
| C05 | Worship hub | + | prayer, qibla, wudu, salah, duas, adhkar, tasbih |
| C06 | More | + | Ramadan, Zakat, Hajj, Calendar, Downloads, Settings… |
| C07 | Global search | [1.6] | Quran / Hadith / Duas / Lessons |

## D · Quran
| D01 | Quran library | [4.1] | Surah / Juz / Favorites / Recent |
| D02 | Juz list | + | 30 juz with start ayah |
| D03 | Quran reader | [4.2] | Arabic + Urdu + English, audio bar |
| D04 | Ayah actions (sheet) | [4.3] | play, bookmark, note, share, tafsir, words, copy |
| D05 | Reader settings (sheet) | + | translations, font size, script, reciter |
| D06 | Full surah player | [4.4] | repeat, speed, sleep timer, download |
| D07 | Reciter picker | + | |
| D08 | Bookmarks & last read | [4.5] | Bookmarks / Notes / Highlights |
| D09 | Add note (sheet) | + | |
| D10 | Tafsir & word meanings | [4.6] | named sources |
| D11 | Share ayah | + | image card preview |
| D12 | Quran search | [4.7] | Arabic / Urdu / English |
| D13 | Noorani Qaida | [4.8] | Lessons / Letters / Tajweed |
| D14 | Hifz practice | [4.9] | Repeat / Hide & reveal / Quiz |
| D15 | Sleep timer (sheet) | + | |

## E · Salah & worship
| E01 | Prayer times | [2.1] | per-prayer adhan toggle |
| E02 | Prayer & adhan settings | + | method, Asr madhab, high latitude, offsets, sound |
| E03 | Choose location | + | GPS or city search (worldwide) |
| E04 | Qibla – compass | [2.2] | |
| E05 | Qibla – map | + | great-circle line to Kaaba |
| E06 | Wudu guide | [2.3] | |
| E07 | Ghusl guide | [2.4] | |
| E08 | Learn Salah | [2.5] | Postures / In detail / Common mistakes, fiqh |
| E09 | Kalimas | [2.6] | |
| E10 | Dua library | [2.7] | |
| E11 | Dua detail | + | Arabic, transliteration, meaning, source |
| E12 | Morning & evening adhkar | [2.8] | counter |
| E13 | Digital tasbih | [2.9] | |
| E14 | Tasbih counters & history | + | |
| E15 | Special prayers | [1.5] | Janazah / Eid / Jummah / Others |
| E16 | Prayer guide steps | + | step-by-step for a special prayer |

## F · Knowledge & character
| F01 | Hadith of the day | [3.1] | |
| F02 | Hadith library | [3.2] | collections |
| F03 | Hadith books & chapters | + | |
| F04 | Hadith detail | [3.3] | grade, narrator, reference |
| F05 | Aaj ki achi baat | [3.4] | |
| F06 | Akhlaq | [3.5] | |
| F07 | Akhlaq topic & scenario | + | |
| F08 | Prophets & Seerah | [3.6] | no depictions of prophets |
| F09 | Story reader | + | |
| F10 | Seerah timeline | + | |
| F11 | 99 Names | [3.7] | |
| F12 | 99 Names grid | + | |
| F13 | Learning roadmap | [5.6] | |
| F14 | Five pillars | [5.7] | |
| F15 | Structured lesson | [3.8] | |
| F16 | Lesson quiz | + | |
| F17 | Lesson complete | + | |
| F18 | Learning progress | [3.9] | |

## G · Family & kids
| G01 | Kids 2–5: Let's learn together | [5.8] | |
| G02 | Kids 6–12 home | + | |
| G03 | Parent dashboard | [5.9] | |
| G04 | Child-safe settings | + | |

## H · Seasons & tools
| H01 | Ramadan hub | [1.1] | |
| H02 | Quran plan (khatam) | + | |
| H03 | Fasting guide | + | |
| H04 | Ramadan tracker | + | |
| H05 | Zakat calculator | [1.2] | |
| H06 | Zakat result & learn | + | |
| H07 | Hajj & Umrah guide | [1.3] | |
| H08 | Hajj checklist | + | |
| H09 | Hajj stage detail | + | |
| H10 | Islamic calendar | [1.4] | Hijri adjust ±2 |
| H11 | Event detail | + | |

## I · Account & settings
| I01 | Profile | [1.9] | |
| I02 | Edit profile | + | |
| I03 | Settings | [1.8] | theme, text size, RTL, language, fiqh, method |
| I04 | Downloads | [1.7] | |
| I05 | Privacy & data | + | export, delete account (store requirement) |
| I06 | Delete account (dialog) | + | |
| I07 | Help & FAQ | + | |
| I08 | Content correction | + | |
| I09 | About & sources | + | authentic sources, licences, scholar review |
| I10 | Saved items | + | |

## J · States
| J01 | Loading | + | |
| J02 | Offline | + | |
| J03 | Empty | + | |
| J04 | Error | + | |
| J05 | Location denied | + | |
| J06 | Adhan notification | + | lock-screen |
