# Connect 4 AI
- AI that always plays the best move
- When in a winning position, it plays the move that results in the quickest, assuming optimal play
- When in a losing position, it plays the move that prolongs tha game as long as possible

## How It Works

### General Overview (More Detailed Explanations Below)
- This AI uses principle variation search with alpha-beta pruning to calculate the best move
- The algorithm is deterministic, meaning it will produce the same result everytime
- The algorithm searches until it finds the move that is guaranteed to be optimal
- Optimizations are made to speed up this search because searching every position in reasonable time is not feasible
- It orders moves to be searched based on how likely they are to be good because the most positions will be pruned when the moves are search in order from best to worst
- Results of previous searches are cached to prevent fully researching positions that are reached more than once

### Minimax/Negamax Algorithm
- The minimax value is a numerical value that represents the outcome of the game starting at the current position if both players play optimally
- In the minimax version, one player tries to maximize the minimax value, and the other players tries to minimize the minimax value
- If the minimax value is positive, the maximizing player is winning. If it is negative, the minimizing player is winning. If it is zero, the game is a theoretical draw
- Since Connect 4 is a zero-sum game, meaning that one player's advantage is equal to the other player's disadvantage, we can use a variation of the minimax algorithm called the negamax algorithm
- We can define the negamax value as positive when the player to move can force a win, negative if the other player can force a win, and 0 if the game is a theoretical draw
- The negamax value of one player will be equal to the negation of the negamax value of the other player
- For example, if one player's negamax value is +5, the other player's negamax value is -5
- The sum of both player's negamax value is always 0, which is why the negamax algorithm only works with 0 sum games
- Since the greatest negamax value is always optimal for the current player making a move, this makes writing the algorithm easier as both player will try to maximize the score rather than having 1 player maximize and 1 player minimize
- Once the negamax value is calculate, we can simply negate it when calculating the negamax value for the other player
- For Connect 4, we define the negamax value based on how many moves it takes to win.
- For a winning position, the move that forces a win in the least amount of moves will have the highest negamax value
- In a losing position, the move that prolongs th game as long as possible wil have the highest negamax value
- The formula for Connect 4 for a winning position is: 22 - (moves to win)
- And for a losing position: -(22 - (moves to win))
- And of course 0 for a theoretical draw
- From above, you can see that the winning and losing formula as the negation of each other, meaning than winning in n moves is equals to the negation of losing in n moves

### Alpha-Beta Pruning
- Alpha-Beta pruning is used to reduce the amount of positions searched by eliminating positions that have no effect on negamax value, as they can only be reached if the gameplay is not optimal
- When searching a position, we always have an alpha value and a beta value
- The alpha value represents the lower bound of the search
- The beta value represents the upper bound of the search, which represents the maximum score the other player (the player who is not making a move at the current position) can achieve by choosing any of the paths that have already been previously search (the opponent's best option so far)
- For the root position (the current position), the alpha value is set to the lowest possible value, and the beta value is set to the highest possible value
- When searching each move, we set the [alpha, beta] bounds for the other player to [-beta, -alpha] because we are using the negamax algorithm
- The above statement can be confusing, so hopefully what I have written below helps
- Player 2's alpha being equal to player 1's -beta means the worst player 2 can achieve is equal to the negation of the best negamax value found so far that player 1 can achieve
- Player 2's beta being equals to player 1's -alpha means the best player 2 can achieve is equal to the negation of the worst negamax value found so far that player 1 can achieve
- After searching each move, alpha becomes the maximum of the current alpha value and the negamax value of the move that was just searched
- Pruning occurs when alpha is greater than or equal to beta, which is called the beta-cutoff
- This means that this position is just as good or worse for the other player, so the other player would simply pick a different move
- A beta-cutoff means that we no longer have to search any more positions down this path as they cannot affect the negamax value of the root position
- To optimize our algorithm, we want beta-cutoffs to occur as frequently as possible because more positions will be pruned
- Searching better moves first is essential to performance because if the best move would make a beta-cutoff, it is searched first, then we only have to search the best move and can ignore other moves

### Principal Variation Search
- Principal Variation Search uses the idea that the exact negamax value is only needed for positions that are in the Principal Variation, or the sequence of moves that result in optimal play
- It fully searches the first move and sets it as the best move found so far
- For every other move, it performs a more efficient search to determine if the current move is better
- If it finds that the current move is better, then it fully searches the current move for it's exact negamax value as sets it as the best move
- This more efficient search is called a null-window search, which is when alpha and beta only differ by 1
- This is generally more efficient because more beta-cutoffs will occur more frequently because a beta-cutoff will occur whenever alpha gets updated
- The current best negamax value will be equal to alpha, so we can perform the null-window search on next moves with the [alpha, beta] bounds: [-alpha -1, -alpha]
- If the null-window search returns -alpha - 1, which becomes alpha + 1 when negated we know that this move is better than the current move found so far because it only had a value of alpha
- The reasons that we have to do a full re-search with bounds [-beta, -alpha] is the value of the move could be greater than alpha + 1 as the null-window search producing a value of alpha + 1 only tells us that the current move is better the best move found so far
- Principal Variation search is most efficient when the best move is searched first because for every other move, we will only have to perform a null-window search on these moves

### Caching Positions
- Positions can be cached to avoid fully re-searching them when they are reached again
- We will have 2 caches, one which stores the lower bound of a position's negamax values, and the other which stores the upper bound of a position's negamax value
- We will find the lower bound of the negamax value when a beta-cutoff occurs because this means that the current position has a negamax value of at least alpha, but it could be higher since we pruned other moves
- If no beta-cutoff occurs, then alpha will be an upper bound of the negamax value because we did not prune any moves so the negamax value cannot be higher than alpha
- When the same position is reached again, we can update alpha to max of alpha and the lower bound stored in the cache, and update beta to the minimum of beta and the value stored in the upper bound cache
- Additionally, we can check all moves and see if they are in the upper bound cache and update alpha to the max of alpha and the negation of their upper bound
- This will reduce the difference between alpha and beta, which will lead to more beta-cutoffs and an overall faster search

### Move Ordering
- Moves that create winning threats are more likely to be better moves, so those are searched first
- Also, winning threats of the same parity of the current player (threats on an even row for the second player, and odd row for first player) are very likely to be good moves so these are given twice the priority as other threats
- The priority of a move is determined as: 2 * (# of winning threats of same parity) + (# of winning threats of opposite parity)
- So, the moves are ordered by highest priority first
- In case of a tie, the move closest to the center are searched first as playing closer to the center is likely to be better than playing closer to the edge

### Efficient Representation Of The Game Board
- This has no effect on the number of positions searched, but can decrease the time spent evaluating each position
- The board can be represented with 63 bits, making it fit inside 1 64-bit integer
- The first 42 bits represents the pieces on the board, marked as a 1 for 1 player and a 0 for the other player
- Bits 0-5 represents the first column, 6-11 represent the second column and so on
- The next 21 bits represents the heights of each column. Since the maximum column height is 6, 3 bits are needed
- Bits 42-44 represent the height of the first column, 45-47 represent the height of the second column, and so on
- This always making moves and checking for wins and threats to be done simply with numerical operations, making it much more efficient than using a data structure such as a 2d array
- Overall, this makes the code faster, but note that optimizations to the negamax algorithm will be more important to improving performance than the way we represent the board
