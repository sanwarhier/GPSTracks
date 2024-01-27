# GPSTracks
Ein Projekt für die Lehrveranstaltung "Computergrafik für GIS" bei Prof. Dr. Marius Appel and der Hochschule Bochum

## Aufgabenbeschreibung
Sie sollen ein Mini-Geovisualisierungssystem entwickeln, das GPS Tracks laden und interaktiv darstellen kann. Das System soll folgende Funktionalität anbieten:

### Anforderungen
+ Über eine grafische Benutzerschnittstelle können GPS Tracks aus Dateien geladen werden.
+ GPS Tracks werden vereinfacht als einzelne CSV Dateien gespeichert, wobei jede Zeile einen Track-Punkt mit Koordinaten, Zeit und weiteren Attributen repräsentiert.
+ Nach Laden eines Tracks, soll er in einer Kartendarstellung durch Linien visualisiert werden.
+ Nach Laden eines Tracks soll der Kartenausschnitt automatisch so gewählt werden, dass der gesamte Track gut sichtbar ist.
+ GPS Tracks sollen basierend auf der Geschwindigkeit eingefärbt werden.
+ Grafische Parameter wie die Liniendicke und Farbe sollen durch Nutzer:innen in der GUI auswählbar sein.
+ Die Kartendarstellung erlaubt Zoom / Pan Interaktionen über die Maus und/oder Tastatur.
+ Die Anwendung soll mit Java2D und dem GUI Framework Swing implementiert werden.

### Bonus-Features
Wählen Sie mindestens eines der folgenden Bonus-Features, um ihre Anwendung zu erweitern.
+ Nutzer:innen können in der GUI auswählen, ob GPS Tracks als Linienzüge oder als CR Splines dargestellt werden.
+ Die Anwendung erlaubt es, eine Hintergrundkarte aus einem Tile Map Service anzuzeigen.
+ Beim Herauszoomen wird der GPS Track mit Hilfe des Douglas-Peucker-Algorithmus generalisiert.

## Ergebnisse
Die Anforderungen wurden umgesetzt. Jedoch ist eine Auswahl der Farbe nicht mehr möglich, da die Farbe je nach Geschwindigkeit gewählt wird.
Das Zoomen und Verschieben passt noch nicht ganz zur Maus-Position, ist aber benutzbar.

Als Bonus-Feature wurde die Hintergrundkarte gewählt. Diese wird aus einem WMS heruntergeladen. Als BBox werden die Ausmaße des Tracks genommen.
Die Position und Skalierung des Tracks und der Karte passen allerdings noch nicht zusammen.

### Hinweis
Das Projekt wurde in Visual Studio Code erstellt und sollte ohne weiteres darin ausführbar sein.
Bei Fragen bitte an mich wenden.


