
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
from pandas import read_csv
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (accuracy_score, balanced_accuracy_score,
                             confusion_matrix)
import json

features_columns = [
    "ctr-method-name",
    "ctr-args-lit",
    "ctr-return-lit",
    "ctr-method-http",
    "srv-method-name",
    "srv-args-lit",
    "srv-return-lit",
    "rep-method-op",
    "rep-args-lit",
    "rep-return-lit",
    "cal-method-http",
    "cal-url",
    "cal-return-lit"
]
label_column = "true-clone"
positive_values = ["A", "B"]
negative_values = ["N"]

sigmoid = np.vectorize(
    lambda x: 1 / (1 + np.e ** (-x)) if x > 0 else 1 - 1 / (1 + np.e ** (x))
)

def get_logistic_regression_classifier(thr=0.5):
    def log_reg_class(X, w):
        val = sigmoid(X @ w)
        ones = np.ones(val.shape)
        return (val >= thr) * ones
    return log_reg_class


def _threshold_predict(model, X, thr=0.5):
    probs = model.predict_proba(X) 
    return (probs[:, 1] > thr).astype(int)


def fit_logistic_regression(*, training_data_file, threshold, save_dir, **_):
    print("Reading data...")
    df = read_csv(training_data_file)
    X_pos = df[df[label_column].isin(positive_values)][features_columns].to_numpy()
    X_neg = df[df[label_column].isin(negative_values)][features_columns].to_numpy()

    X = np.concatenate([X_pos, X_neg])
    y = np.concatenate([np.ones(X_pos.shape[0]), np.zeros(X_neg.shape[0])])

    print("Fitting model...")
    log_reg = LogisticRegression(class_weight="balanced")
    log_reg.fit(X,y)

    print("Model weights:")
    print(list(zip(features_columns, log_reg.coef_)))

    y_pred = _threshold_predict(log_reg, X, threshold)

    print("Computing evaluation metrics...")
    acc = accuracy_score(y, y_pred)
    bal_acc = balanced_accuracy_score(y, y_pred)
    conf_matrix = confusion_matrix(y, y_pred)

    
    print(f"Balanced accuracy: {bal_acc}")
    print(f"Real accuracy: {acc}")
    print("Confusion matrix")
    print(conf_matrix)

    print("Saving experiment artifacts...")
    fig, ax = plt.subplots(figsize=(7.5, 7.5))
    ax.matshow(conf_matrix, cmap=plt.cm.Blues, alpha=0.3)
    for i in range(conf_matrix.shape[0]):
        for j in range(conf_matrix.shape[1]):
            ax.text(x=j, y=i,s=conf_matrix[i, j], va='center', ha='center', size='xx-large')
    ax.set_xticks(range(2), ["Non-clone", "Clone"])
    ax.set_yticks(range(2), ["Non-clone", "Clone"])
    
    plt.xlabel('Predictions', fontsize=14)
    plt.ylabel('Actuals', fontsize=14)
    plt.title('Confusion Matrix', fontsize=18)

    save_dir = Path(save_dir)

    # Save confusion matrix
    fig.savefig(save_dir / "confusion_matrix.png")
    print(log_reg.intercept_)
    model = {
        "coef": log_reg.coef_[0].tolist(),
        "intercept": log_reg.intercept_[0],
        "threshold": threshold,
    }

    # Save model parameters
    (save_dir / "model.json").write_text(json.dumps(model))


def predict_on_csv(*, model_json, prediction_files, save_dir, **_):
    save_dir = Path(save_dir)

    for data_file, save_filename in prediction_files:
        print(f"Predicting on: {data_file}")
        
        df = read_csv(data_file)
        X = df[features_columns].to_numpy()
        X = np.concatenate([np.ones((X.shape[0], 1)), X], axis=1)
        model_dic = json.loads(Path(model_json).read_text())
        w = np.array([model_dic["intercept"]] + model_dic["coef"])
        
        log_reg = get_logistic_regression_classifier(thr = model_dic["threshold"])
        y_pred = log_reg(X,w)

        names = []
        for _, row in df.iloc[y_pred==1].iterrows():
            names.append((row["cfg 1"], row["cfg 2"]))
        
        save_file = save_dir / save_filename
        save_file.write_text(json.dumps(names))


if __name__ == "__main__":
    from argparse import ArgumentParser

    parser = ArgumentParser()

    global_parser = ArgumentParser(description="Semantic code clone detection")
    subparsers = global_parser.add_subparsers(title="subcommands")

    # TRAIN MODEL CLI

    train_parser = subparsers.add_parser(
        "train", help="Fit a logistic regression model and store the results"
    )
    train_parser.set_defaults(func=fit_logistic_regression)

    train_parser.add_argument("-d", "--training_data_file", type=str, required=True)
    train_parser.add_argument("-s", "--save_dir", type=str, required=True)
    train_parser.add_argument("-t", "--threshold", type=float, default=0.99)

    predict_parser = subparsers.add_parser(
        "predict", help="Run predictions using a previously trained model on a list of files and dump the results"
    )
    predict_parser.set_defaults(func=predict_on_csv)
    predict_parser.add_argument("-m", "--model_json", type=str, required=True)
    predict_parser.add_argument("-f", "--prediction_files", nargs=2, type=str, action="append", required=True)
    predict_parser.add_argument("-s", "--save_dir", type=str, required=True)

    args = global_parser.parse_args()
    args.func(**vars(args))