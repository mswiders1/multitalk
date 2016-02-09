a#summary Komunikaty przesyłane przez sieć

# UWAGA! #

**Wszystkie** komunikaty po TCP/IP rozpoczynają się ciągiem znaków:

```
BEGIN_MESSAGE:<długość_wiadomości>\n
<wiadomość_JSON>
```

gdzie:
  * `<długość_wiadomości>` - długość wiadomości **po** znaku nowej linii
  * `<wiadomośi_JSON>` - wiadomość w postacji JSON zgodnie z opisem poniżej

Powyższe jest niezbędne do rozpoznania początku i końca pojedynczego komunikatu


# Podłączenie (broadcast) #

Protokół: **UDP**, broadcast

Zawartość: _MULTITALK\_5387132_

Ilość: 3 razy, co sekundę jeden


# Podłączenie (p2p) #

Protokół: **TCP/IP**

Odbiorca: węzeł podany przez użytkownika

Zawartość:

| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _P2P_       |


# Podłączenie - odpowiedź #

Protokół: **TCP/IP**

Odbiorca: węzeł podłączający się



| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _HII_       |
| UID            | CHAR(32) |H(MAC + IP + username_) SHA-1 base 64_|
| USERNAME       | VARCHAR(32) |  wujo008    |
| VECTOR         | VEC     | _tablica_   |


Zawartość: tablica informacji o wszystkich użytkownikach (**JSON**)

oraz typ wiadomości i informację o przesysłającym

Postać jednego rekordu tablicy:

| **nazwa pola** | **typ** |
|:---------------|:--------|
| IP\_ADDRESS    | VARCHAR(15) |
| UID            | CHAR(32) |
| USERNAME       | VARCHAR(32) |

Przykład:

```
{
    "TYPE": "HII",
    "UID": "8IsXHf84O63uktYcIto2wk+h4sw=",
    "USERNAME": "kouodziey",
    "VECTOR": 
        [
            {
                "IP_ADDRESS": "192.168.0.5",
                "UID": "3uktYcIto2saFasD=",
                "USERNAME": "wujo"
            },         
            {
                "IP_ADDRESS": "192.168.0.6",
                "UID": "8IsXHf84O63uktYcIto2wk+h4sw=",
                "USERNAME": "kouodziey"
            }         

        ]
}

```

# Logowanie #

Protokół: **TCP/IP**

Odbiorca: wszyscy użytkownicy, z którymi logujący się węzeł ma połączenie

Zawartość: zestaw informacji o użytkowniku (**JSON**)

| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _LOG_       |
| UID            | CHAR(32) | _H(MAC + IP + username_) |
| USERNAME       | VARCHAR(32) |             |
| IP\_ADDRESS    | VARCHAR(15) | adres ip    |

# Przeslanie macierzy wiedzy do nowozalogowanego usera #

Protokół: **TCP/IP**

Odbiorca: Uzytkownik ktory sie dopiero zalogowal

Zawartosc: Informacje o wiedzy

| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _MTX_       |
| MAC            | MATRIX  | macierz     |
| VEC            | VECTOR  | wektor kolejnosci |

macierz:

wierszami wiedza użytkowników

pojedyncza komórka: wiedza o wiedzy o zegarze logicznym użytkowników

wektor kolejności:
| id usera, który ma wiedzę w i-tym wierszu tabeli|
|:------------------------------------------------|

# przesylanie zwyklych wiadomosci #

Protokół **TCP/IP**
Odbiorca: uzytkownik do ktorego jest skierowana wiadomosc

Zawartość: Macierze wiedzy i zwykła treść wiadomości

| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _MSG_       |
| SENDER         | CHAR(32) | H(MAC + IP + username_) SHA-1 base 64_|
| RECEIVER       | CHAR(32)| H(MAC + IP + username_) SHA-1 base 64 - moze byc puste_|
| MSG\_ID        | INT     | zegar logiczny|
| TIME\_VEC      | VECTOR  | wektor wartości zegarów logicznych użytkowników |
| VEC            | VECTOR  | wektor kolejnosci |
| CONTENT        | STRING  | Hello world!|

pojedyncza komorka wektora zegarów: wartość zegara logicznego użytkownika

wektor kolejności: UID usera, którego wartość zegara logicznego jest na i-tej pozycji wektora zegarów

# LIV message #

Protokół **TCP/IP**
Odbiorca: wszyscy użytkownicy

Wysyłany co 10 sekund.

Zawartość:


| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _LIV_       |
| UID            | CHAR(32) | H(MAC + IP + username_) SHA-1 base 64_|
| IP\_ADDRESS    | VARCHAR(15) | adres ip    |
| SEQUENCE       | NUMBER  | kolejne numery |


# GET message #

Protokół **TCP/IP**
Odbiorca: wszyscy użytkownicy

Pobieranie wiadomości wysłanej przez podanego użytkownika o podanym numerze.

Zawartość:


| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _GET_       |
| UID            | CHAR(32) | H(MAC + IP + username_) SHA-1 base 64_|
| MSG\_ID        | INT     | numer wiadomości (zegar logiczny) |


# OUT message #

Protokół **TCP/IP**
Odbiorca: wszyscy użytkownicy

Informacja o wylogowaniu

Zawartość:


| **nazwa pola** | **typ** | **wartość** |
|:---------------|:--------|:------------|
| TYPE           | CHAR(3) | _OUT_       |
| UID            | CHAR(32) | H(MAC + IP + username_) SHA-1 base 64_|