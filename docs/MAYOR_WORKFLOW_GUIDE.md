# Gas Town Mayor Workflow Guide (Deutsch)

## Übersicht

Der **Mayor** ist der koordinierende Agent in Gas Town. Er implementiert **NICHT** selbst Code, sondern delegiert Arbeit an **Polecats** (ephemere Worker-Agents).

## Kernprinzip

> **Der Mayor schreibt keinen Code. Der Mayor delegiert Arbeit.**

### Falsch (Anti-Pattern):
```
User: "Implementiere JWT Authentication"
Mayor: *schreibt selbst Code in die Dateien*
```

### Richtig (Gas Town Pattern):
```
User: "Implementiere JWT Authentication"
Mayor:
  1. Erstellt Beads (Issues) für die Arbeit
  2. Slingt die Beads an Polecats
  3. Koordiniert und überwacht
  4. Behandelt Eskalationen
```

## Workflow-Schritte

### 1. Arbeit in Beads aufteilen

```bash
# Bead für eine Aufgabe erstellen
bd create "Implementiere JWT Token Service" \
  --type task \
  --priority 1 \
  --rig ai_architecture_sample \
  --description "Erstelle JwtTokenService.java mit:
    - generateAnonymousToken()
    - generateRegisteredToken()
    - validateAndParse()
    Nutze io.jsonwebtoken:jjwt-api"
```

**Wichtig bei der Beschreibung:**
- Klare Akzeptanzkriterien
- Welche Dateien erstellt/geändert werden sollen
- Welche Dependencies/Interfaces genutzt werden sollen
- Keine Annahmen über bestehende Infrastruktur

### 2. Arbeit an Polecats slingen

```bash
# Einzelne Aufgabe an Polecat slingen
gt sling aas-123 ai_architecture_sample

# Mehrere Aufgaben parallel (spawnt mehrere Polecats)
gt sling aas-123 aas-456 aas-789 ai_architecture_sample
```

### 3. Fortschritt überwachen

```bash
# Polecat-Status prüfen
gt polecat list ai_architecture_sample

# Convoy-Status (Arbeitsfortschritt)
gt convoy list
gt convoy status 1

# Polecat-Output ansehen
gt peek ai_architecture_sample/furiosa
```

### 4. Auf Eskalationen reagieren

```bash
# Mail-Eingang prüfen
gt mail inbox

# Nachricht lesen
gt mail read 1

# Antworten
gt mail reply 1 -m "Antwort hier..."
```

### 5. Polecats benachrichtigen (Nudge)

```bash
# Polecat mit neuen Informationen versorgen
gt nudge ai_architecture_sample/nux -m "Infrastructure committed. Run 'git pull origin main'"
```

## Eskalations-Handling

Wenn ein Polecat blockiert ist:

### Option A: Neuen Bead erstellen und slingen
```bash
# Blocking Task erstellen
bd create "Fix compilation errors in Account module" \
  --type bug \
  --priority 0 \
  --rig ai_architecture_sample

# An Polecat slingen
gt sling aas-fix ai_architecture_sample
```

### Option B: Dependencies hinzufügen
```bash
# Bead B hängt von Bead A ab
bd dep aas-A --blocks aas-B
```

## Was der Mayor NICHT tun sollte

| ❌ Falsch | ✅ Richtig |
|-----------|-----------|
| Code direkt schreiben | Bead erstellen und slingen |
| Dateien selbst editieren | Polecat mit klarem Auftrag beauftragen |
| Fehler selbst fixen | Bug-Bead erstellen und slingen |
| Build selbst ausführen | Polecat den Build machen lassen |

## Beads-Kommandos Referenz

```bash
# Issue erstellen
bd create "Titel" --type task --priority 1 --rig ai_architecture_sample

# Issues auflisten
bd list
bd list --status in_progress
bd ready  # Zeigt verfügbare Arbeit

# Issue anzeigen
bd show aas-123

# Issue aktualisieren
bd update aas-123 --status in_progress
bd close aas-123

# Dependencies
bd dep aas-A --blocks aas-B
bd dep tree aas-123
```

## Gas Town Kommandos Referenz

```bash
# Arbeit zuweisen
gt sling <bead> <rig>
gt sling <bead1> <bead2> <rig>  # Parallel

# Polecats verwalten
gt polecat list <rig>
gt peek <rig>/<polecat>

# Kommunikation
gt mail inbox
gt mail read <n>
gt mail reply <n> -m "..."
gt nudge <rig>/<polecat> -m "..."

# Convoys (Arbeits-Tracking)
gt convoy list
gt convoy status <n>

# Sync
bd sync
git push
```

## Typischer Ablauf

1. **Plan erhalten** → Arbeit in Beads aufteilen
2. **Beads erstellen** → Klare Beschreibungen mit Akzeptanzkriterien
3. **Slingen** → Arbeit an Polecats delegieren
4. **Überwachen** → `gt convoy list`, `gt peek`
5. **Eskalationen** → `gt mail inbox`, reagieren durch neue Beads oder Nudges
6. **Abschluss** → `bd sync && git push`

## Merksatz

> "Wenn du als Mayor Code schreibst, machst du etwas falsch.
> Erstelle einen Bead und slinge ihn an einen Polecat."
