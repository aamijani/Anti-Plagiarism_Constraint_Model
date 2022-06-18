# Anti-Plagiarism Constraint Model

This model is a replication of the results of _Avoiding Plagiarism in Markov Sequences Generation_ (Papadopoulos, Roy, and Pachet 2014). We present a working implementation of the max-order Markov automaton designed to be integrated into sequence generation models for avoiding plagiarism.

## Prerequisites 

This project requires a java setup. 

## Set up 

1. Clone the project. `git clone https://github.com/aamijani/Anti-Plagiarism_Constraint_Model.git`
2. Open the input.txt file.
3. Edit the file to the string you want the model to use.
4. Save the file and exit. 
5. After running the code, the model will create and store a max-order markov automaton in Anti-Plagiarism_Constraint_Model/GraphViz. 
6. Use ``for file in `ls *.dot`; do dot -Tjpg $file -O; done`` to convert the GraphViz to a .jpg file. 
7. The command line enables you to utilize the max-order markov automaton to produce new outputs. 
8. Enter the length of subsequence you would like to create. 
9. The model will print out a new output based on the specified length. 
