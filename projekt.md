# Projekt rozwiązania #

Na tej stronie znajdują się ustalenia co do implementacji zadania.

## Notatki ze spotkania 2010-12-19 ##

### Proces podłączenia do systemu ###

Założenie: unikalność **H(MAC + IP + username)** - traktowane jako UID

  1. Wysłanie po broadcast-cie (UDP) zapytania o osiągalne hosty
  1. Jeżeli nikt nie odpowiedział, to połączenie z 1 węzłem po TCP/IP podanym explicite przez użytkownika
  1. Otrzymanie listy użytkowników od każdego węzła, z którym mam połączenie
  1. Próba połączenia do każdego użytkownika
  1. Wysłanie do wszystkich informacji o podłączeniu z UID i oczekiwanie na ACK od każdego

### Proces wysyłania wiadomości ###

  1. Jeżeli istnieje bezpośrednie połączenie między użytkownikami, to wiadomość jest wysyłana i oczekiwanie na ACK
  1. Jeżeli jest ACK - wiadomość została dostarczona i można banglać dalej
  1. Jeżeli wystąpił timeout na ACK, to wiadomość jest wysyłana do wszystkich węzłów z którym mam połączenie i oczekuję na ACK

### Szczegóły implementacyjne ###

  * Defaultowy port: 3554 z możliwością zmiany przez użytkownika
  * Wykorzystanie JSON-a jako formatu przesyłu danych
  * UID użytkownika: wartość funkcji skrótu z adresu MAC, adresu IP oraz tekstowej nazwy użytkownika
  * [Struktura komunikatów](komunikaty.md)

## Notatki ze spotkania 2011-01-17 ##

Ustalone zmiany i rozszerzenia:
  * wysyłanie broadcastu 3 razy, co 1 sekundę
  * dodanie adresu IP w komunikacie LOG message
  * w komunikacie MSG - wektor zegarów logicznych zamiast macierzy
  * komunikat LIV - alive - co 5 sekund, do wszystkich
  * komunikat GET - pobranie brakującej wiadomości
  * komunikat OUT - logout