## Code

In this problem you will write a function to navigate complex multi-level structures that are based
on the ability to crack MD5 hashes. The numbers resulting from the sequence of encountered simple hashes
will compose the key to decode the secret location of the treasure.

## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

a) Extend your Pirate.java class to navigate a multi-level treasure map, build up the list of hints and
then use the full list of hints to discover the instructions to locate the treasure!

As usual the starting point is a list of hashes. Some of these hashes can be cracked right away (simple
hints), while others can be cracked with compound hints. The main difference compared to HW7 is
that before we only had two processing stages: (1) initial stage for simple hints; (2) second stage for
compound hints. Now, we might have multiple subsequent processing stages that build on the result
of the previous stages. Let us review this concept.

Starting from the first stage, we first attempt to crack the input hashes as if they are simple hints and
use the same timeout mechanism to detect so-far uncrackable hashes (just like in HW7). This leads
to a first list of integers L0 = {A, B, C, D, . . .}. Next some of the hashes that could be not previously
cracked might be crackable with a compound hint of the form “α; k; β” generated in the same way as
we previously did—and with the same constraints between α, k, and β.

Completing the processing described above leads to the definition of a new list of integers L1 =
{k1, k2, k3, . . .} where each ki comes from a different compound hint. Note that none of the integers
in L0 can show up in L1. Using the new list of integers some more (or potentially all) of the so-far
uncrackable hashes can be cracked with a new compound hint built in the same way as before. That
is, using a compound hint of the form “kx; h; ky” where kx < h < ky and kx, ky ∈ L1. If after this
step there are still uncrackable hashes, one more round of processing is needed by defining a new list
of integers L2 etc. In this problem, all the input hashes are crackable! Thus, a valid solution should
not leave any hash uncracked.

Consider now the various list of integers L0, L1, L2, . . . constructed in the multiple processing stages.
To find the instructions for the treasure, we need to take the union of all the integers in the list, and
then sort the integers in ascending order. Finally, each number is the offset of a single character inside
a cipher-text file passed in input.

Your job is to print, in order, the corresponding characters in the second file at the correct offsets. The
resulting string will provide the final directions to find the treasure. Let us make a full example. Let’s
say that the input list of hashes is:

c81e728d9d4c2f636f067f89cc14862c
c9f0f895fb98ab9159f51fd0297e236d
d3d9446802a44259755d38e6d163e820
c51ce410c124a10e0db5e4b97fc2af39
cc397dd8486d3b0aec12fd25c76b19e2
3e66ce357af2da396c17631e706f1941
dd6cf119df0327fd2fc7500985284f59

After the first stage we have the intermediate result below, so L0 = {2, 8, 10, 13}:
2
8
10
13
cc397dd8486d3b0aec12fd25c76b19e2
3e66ce357af2da396c17631e706f1941
dd6cf119df0327fd2fc7500985284f59

Next with the integers in L0 we are able to crack two more hashes, so the partial result is the one
below and L1 = {5, 11}:
2;5;10
8;11;13
dd6cf119df0327fd2fc7500985284f59

Finally, the remaining hash can be cracked with the compound hash below:
5;7;11

Putting all the integers from the lists together, we have the following list of numbers: {2, 5, 7, 8, 10, 11, 13}.

Now if the content of the input cipher-text is:
ljDshoDne{ :S)43 298r76Qt0

The solution is the string: “Done :)”, comprised by the characters in the position indicated by the
integers in the final list.

Extend the Java class Pirate.java that defines a method called findTresure(...) that is able to
find the location of the treasure as described above. The only output of the code should be the final
instructions to find the treasure. Apart from implementing the findTreasure(...) method, the
class should also include a public static void main(String [] args) function. The main(...)
function should accept 4 parameters from the calling environment. (1) The path to the input hash
list is passed as the first parameter. (2) The second parameter passed to your code is the number of
available CPUs. (3) The third parameter encodes the length of the timeout for (currently) uncrackable
hashes expressed in milliseconds. (4) The fourth parameter is the path to the cipher-text to decode to
find the final instructions for the treasure.
