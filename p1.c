#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>
#include <time.h>

// Questions :
// 1. Types of the following variables are:
//      A - Integer
//      B - Integer
//      X - Integer
//      P - A pointer to a contiguous allocated memory of Integers(points to the first element of the contiguous memory).
// 2. We are using the clock() function from within time.c library
// 3. We must use math.h in order to have access to the sqrt() method.

bool is_prime(int X) {
    for (int i = 3; i <= floor(sqrt(X)); i += 2) {
        if (X % i == 0) {
            return false;
        }
    }
    return true;
}

int main(int argc, char **argv) {
    int A = atoi(argv[1]);
    int B = atoi(argv[2]);
    int P_LEN = (B - A) / 2 + 1; // the length can't be more than half the interval = 1
    int* P = (int*)malloc(P_LEN * sizeof(int));
    int P_NEW_LEN = 0;
    long START_EXECUTION_TIME = clock();

    if (A <= 2) {
        *(P+P_NEW_LEN) = 2;
        ++P_NEW_LEN;
        A = 3;
    }

    for (int X = A; X < B; X += 2) {
        if (is_prime(X)) {
            *(P+P_NEW_LEN) = X;
            ++P_NEW_LEN;
        }
    }

    long END_EXECUTION_TIME = clock();

    printf("Prime numbers in interval [%d,%d] are: \n", atoi(argv[1]), B);
    for(int i = 0 ; i < P_NEW_LEN; ++i)
        printf("%d\n", P[i]);
    
    printf("Execution time in ms: %lf \n", (double)(END_EXECUTION_TIME - START_EXECUTION_TIME) / CLOCKS_PER_SEC * 1000);

    return 0;
}