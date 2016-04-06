#!/bin/bash
#SBATCH --time=01:00:00
#SBATCH --nodes=1
#SBATCH --ntasks=1
#SBATCH --cpus-per-task=1
#SBATCH --mem-per-cpu=10G
#module load Python/3.4.3-goolf-2015a
module load Java/1.8.0_71
module load mpj/0.44
#mpirun -np 4 helloworld.py
javac -cp .:$MPJ_HOME/lib/mpj.jar Main.java Preprocess.java ResultEntity.java
mpjrun.sh -np 1 Main
