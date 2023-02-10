# Semantic clones prediction

This is a CLI of a Logistic-Regression-based semantic clone prediction

There are two commands: `train` and `predict`

## Train a logistic regression model from vectors of similarity features

`python script.py train -d "example.csv" -s "./example_dir"`

## Predict on new data using a previously trained model
`python sem_clone.py predict -m "./example_dir/model.json" -f "predict1.csv" "predict1_output.json" -f "predict2.csv" "predict2_output.json" -s "./example_dir"`

**Note**: You can add multiple -f option, each composed by two strings: one is the csv file with the input features, and the other is the name of the corresponding output file.

**Note**: Make sure directories you pass to the scripts exist before calling this CLI.