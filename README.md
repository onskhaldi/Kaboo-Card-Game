<img width="259" height="195" alt="Screenshot 2026-04-24 at 5 48 41 PM" src="https://github.com/user-attachments/assets/9e039393-4757-442b-87e9-932996130413" />

How to Play Kaboo: The Card Game
Kaboo is a strategic 2-player card game played with a standard 52-card deck. The goal is to manage your grid of cards in such a way that you end up with the lowest possible score by the end of the game.

Setup:
<img width="1270" height="711" alt="Screenshot 2026-04-24 at 5 48 55 PM" src="https://github.com/user-attachments/assets/c1553e2a-ecc2-406e-98f2-791568b0f5f3" />


Each player begins with a 2×2 grid of face-down cards (4 cards per player).


The remaining cards are placed in a draw pile.


A discard pile will also be created during play as cards are discarded.


Objective:
The objective of the game is to have the lowest total score in your 2×2 grid by the end of the game.

<img width="194" height="193" alt="Screenshot 2026-04-24 at 6 00 49 PM" src="https://github.com/user-attachments/assets/2d05f731-6718-43a2-8a85-0caaa04acf60" />


Turn Actions:
On their turn, a player can choose one of the following actions:


Draw from the deck:

<img width="939" height="532" alt="draw" src="https://github.com/user-attachments/assets/e19cd6c1-bd71-467f-a51b-66058d90d93d" />

If the card drawn is a normal card (2-6, or King):
You must either swap it with one of your cards in the grid or discard it.


If the card is a power card (7, 8, 10, Jack, or Queen):
You can choose to either use its effect or swap/discard it.

<img width="940" height="530" alt="Screenshot 2026-04-24 at 5 58 30 PM" src="https://github.com/user-attachments/assets/64f0e65d-fb91-49cf-b06c-fbf1cfe09445" />

<img width="941" height="532" alt="Screenshot 2026-04-24 at 5 58 54 PM" src="https://github.com/user-attachments/assets/ff32130c-c291-4d11-bcb8-da78e74b60ee" />
<img width="942" height="532" alt="Screenshot 2026-04-24 at 5 59 22 PM" src="https://github.com/user-attachments/assets/fffc33e9-bc7a-487d-b6d7-e81922e51be4" />



Draw from the discard pile:


You can choose to draw from the discard pile and swap the card with one of your grid cards.





Knock:

<img width="939" height="529" alt="knok" src="https://github.com/user-attachments/assets/1c8de88f-8b07-487a-ade6-99f072e7302f" />

End the round immediately. All other players get one final turn before the scoring phase begins.





Power Cards and Their Effects:


7/8: Peek at one of your own cards. You can see the value of one of your cards without revealing it to your opponent.


9/10: Peek at one of your opponent’s cards. You can see one of their cards without them knowing.


Jack: Swap one of your cards blindly with one of your opponent's cards. This is a strategic move to disrupt your opponent’s grid.


Queen: Peek at one of your cards and one of your opponent's cards, then optionally swap them. This gives you more information and control over your strategy.



Scoring:


Ace = 1 point


Number cards (2-10) = Face value of the card


Jack / Queen = 10 points each


King = -1 point (negative points are good!)



Ending the Game:


The game ends when a player knocks, signaling the end of the round. All players then get one final turn to adjust their grids before scoring is tallied.


Alternatively, if the deck runs out of cards, the game ends.



Winner:


The player with the lowest total score in their 2×2 grid at the end of the game wins.



Tech Stack / Implementation:


Language: Kotlin


Build Tool: Gradle


Game Engine: BGW (Board Game World)


Testing: JUnit5


Mode: Hotseat (2-player local mode)


UI: GUI logic for an engaging and interactive experience.


With this tech stack, Kaboo is designed to provide a smooth, dynamic experience for players, all while ensuring stability and ease of use.
