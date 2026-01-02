Hier is het visuele meesterwerk voor docs/03_ux_ui_design.md.

We gaan voor een stijl die ik "Cyber-Minimalism" noem. Denk aan de precisie van Formule 1 graphics gecombineerd met de donkere esthetiek van een terminal, maar dan super gepolijst. Het moet voelen alsof je een krachtige computer in je hand houdt.

Inhoud voor docs/03_ux_ui_design.md
Markdown

# 03. UX/UI & Visual Guidelines

Dit document beschrijft de visuele identiteit van MatchMind AI.
**Kernfilosofie:** "Complexiteit verborgen achter pure elegantie." De app moet voelen als een high-tech tool: snel, donker, en haarscherp.

## 1. Visual Identity (The Look)

Wij hanteren een strikt **Dark Mode First** design.

### A. Kleurenpalet (Cyber-Minimalism)
We gebruiken diepe contrasten om de data te laten "poppen".

* **Background (The Void):** `#0F1115` (Bijna zwart, met een vleugje blauw/grijs voor diepte).
* **Surface (The Plate):** `#1E222A` (Voor cards en inputs).
* **Primary Accent (Neon Data):** `#00E676` (Fel groen). Staat voor: Actie, Winst, AI intelligentie.
* **Secondary Accent (Cyber Blue):** `#2979FF` (Voor secundaire info/links).
* **Text High:** `#FFFFFF` (Puur wit, voor headers en uitslagen).
* **Text Medium:** `#B0BEC5` (Lichtgrijs, voor de analyse tekst).
* **Error:** `#EF5350` (Zacht rood, niet agressief).

### B. Typography
* **Headlines:** *Sans-Serif, Bold/Black*. Strak en modern (bijv. Google Fonts 'Inter' of 'Roboto').
* **Data & Numbers:** *Monospace*. Voor percentages en scores (geeft een technisch/analytisch gevoel).
* **Body:** *Sans-Serif, Regular*. Goed leesbaar, ruime regelafstand (1.5).

### C. Iconografie
* Stijl: Rounded, Filled.
* Gebruik spaarzaam iconen, alleen waar ze functie toevoegen (bijv. een 'Settings' gear, een 'Brain' voor de AI).

---

## 2. User Experience (The Feel)

### A. De "Flow"
De gebruiker komt voor één ding: een voorspelling. Leid ze daar direct heen.
1.  **Opening:** Geen splash screen die ophoudt. Direct naar de actie.
2.  **Input:** Vloeiende invoer.
3.  **Wachten:** Spanningsopbouw (Animerende AI status).
4.  **Resultaat:** De "Reveal" (Kaart schuift in beeld).

### B. Feedback & Motion
* **Micro-interacties:** Knoppen hebben een voelbare 'klik' (ripple effect) en subtiele haptic feedback (trilling).
* **Loading State:** GEEN standaard draaiend cirkeltje.
    * *Design:* Een pulserende gloed of een tekst die verandert: *"Verbinding maken..."* -> *"Stats ophalen..."* -> *"Analyseren..."*.
* **Transitions:** Schermen faden niet gewoon, elementen schuiven (slide-in) op hun plek.

---

## 3. Core Components (De Bouwstenen)

### A. De "Arena" (Input Fields)
Dit zijn geen standaard invulvelden. Het zijn de twee strijders.
* **Layout:** Twee grote blokken onder elkaar (of naast elkaar op tablets).
* **Styling:** Donkere achtergrond (`Surface`), lichte border die oplicht (`Primary`) als je erin typt.
* **Placeholder:** "Thuisploeg (bijv. Ajax)" in grijs.

### B. De "Trigger" (Actieknop)
De belangrijkste knop van de app.
* **Positie:** Onderaan het scherm (of direct onder inputs), over de volle breedte.
* **Stijl:** Solid `Primary Color` (Groen) met zwarte tekst (Hoog contrast = Leesbaarheid).
* **Status:**
    * *Disabled:* Donkergrijs (als velden leeg zijn).
    * *Loading:* Toont een horizontale progress bar in de knop zelf.

### C. De "Insight Card" (Het Resultaat)
Dit verschijnt na de berekening.
* **Vorm:** Een Card met afgeronde hoeken (16dp). Licht zwevend effect (elevation).
* **Header:**
    * Links: Naam Winnaar (Groot, Wit).
    * Rechts: Confidence Badge (Pil-vormig, Groene achtergrond, Zwarte tekst: "78%").
* **Body:**
    * Hier staat de tekst van de AI.
    * *Design:* Typewriter effect (de tekst verschijnt letter voor letter heel snel) voor dat "AI gevoel".

### D. De "Vault" (Settings Dialoog)
Voor het invoeren van de API Key.
* Moet veilig en vertrouwd voelen.
* Bevat een link "Waar vind ik mijn key?" die naar DeepSeek docs wijst.
* Input veld is gemaskeerd (******) met een 'oogje' om te tonen.

---

## 4. Accessibility & States
* **Contrast:** Alle teksten voldoen aan WCAG AA standaarden op de donkere achtergrond.
* **Touch Targets:** Alle knoppen zijn minimaal 48dp hoog.
* **Empty States:** Als de app start, is het scherm niet leeg. Toon een subtiele placeholder of een welkomsttekst: *"Welke wedstrijd analyseren we vandaag?"*
