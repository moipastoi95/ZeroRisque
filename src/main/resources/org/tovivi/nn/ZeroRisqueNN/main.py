import os
import sys

import numpy as np
from keras import layers
from tensorflow import keras
import mlp
from PlotLearning import PlotLearning


#####################################################################################################

# Tests sur MNIST Fashion
def test():
    model = keras.Sequential([
        layers.Dense(64, activation='relu', input_shape=(784,)),
        layers.Dense(64, activation='relu'),
        layers.Dense(10, activation='softmax')
    ])

    # Compile the model
    model.compile(optimizer='adam',
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])

    # print biais

    print(model.layers[0].get_weights()[1])

    # Load the MNIST dataset
    (x_train, y_train), (x_test, y_test) = keras.datasets.mnist.load_data()

    # Preprocess the data

    x_train = x_train.reshape(60000, 784).astype('float32') / 255
    x_test = x_test.reshape(10000, 784).astype('float32') / 255
    y_train = keras.utils.to_categorical(y_train, 10)
    y_test = keras.utils.to_categorical(y_test, 10)

    # Train the model
    history = model.fit(x_train, y_train, callbacks=[PlotLearning()], epochs=5, batch_size=64,
                        validation_data=(x_test, y_test))

#test()
#######################################################################################################################

srcPath = "src/main/resources/org/tovivi/nn/"
targetPath = "target/classes/org/tovivi/nn/"

models = []

#######################################################################################################################

# Converters (save / load_model / load data)

configPath = srcPath + sys.argv[1]
modelsPath = configPath + "_models/"
dataPath = targetPath + sys.argv[1] + "_models/"

def save(model):
    for i in range(len(model.layers)):
        path = modelsPath + model.name + "/layer_" + str(i)
        print(path)
        if not os.path.isdir(path):
            os.makedirs(path)
            print("created folder : ", path)
        else:
            print(path, "folder already exists.")
        np.savetxt(path + "/weights", model.layers[i].get_weights()[0], delimiter=",")
        np.savetxt(path + "/bias", model.layers[i].get_weights()[1], delimiter=",")


def load_model(model):
    path = modelsPath + model.name
    if not os.path.isdir(path):
        print(model.name, " model has never been saved...")
        save(model)
        print(model.name, " successfully saved !")
    else:
        print("Starting to load the ", model.name, " model...")
        for i in range(len(model.layers)):
            path = modelsPath + model.name + "/layer_" + str(i)
            w = np.loadtxt(path + "/weights", delimiter=",")
            b = np.loadtxt(path + "/bias", delimiter=",")
            model.layers[i].set_weights([w, b])
        print(model.name, " successfully loaded !")


def load_data(modelName, goal):
    path = dataPath + modelName + "/" + goal
    print("Loading data..." + path)
    pathY = path + "/y"
    pathX = path + "/x"
    y = np.loadtxt(pathY, delimiter=",")
    x = np.loadtxt(pathX, delimiter=",")
    print("Data loaded !")
    return x, y


########################################################################################################################

def config():
    with open(configPath, "r") as f:
        for line in f:
            if (not line.startswith("#")) and (line.strip() != ""):
                # Split the line by the separator ":"
                parts = line.strip().split(":")
                # parts[0] : model name ; parts[1] shape ; parts[2] activations
                shape = list(map(int, parts[1].split(",")))
                activations = list(parts[2].split(","))
                model = mlp.createMLP(shape, activations, parts[0])
                load_model(model)
                models.append(model)
    return models


########################################################################################################################

## Train functions

def trainAndSee(model):
    x_train, y_train = load_data(model, "train")
    x_test, y_test = load_data(model, "test")
    cbs = [PlotLearning()]
    results = model.fit(x_train, y_train, callbacks=cbs, epochs=100, batch_size=64, validation_data=(x_test, y_train))

def optiTrain(model, patience, batchsize):
    x_train, y_train = load_data(model, "train")
    x_test, y_test = load_data(model, "test")
    cbs = [keras.callbacks.EarlyStopping(
        monitor='val_loss',
        min_delta=0,
        patience=patience,
        verbose=0,
        mode='auto',
        baseline=None,
        restore_best_weights=False,
        start_from_epoch=0
    )]
    results = model.fit(x_train, y_train, callbacks=cbs, batch_size=batchsize, validation_data=(x_test, y_train))


def trainAll(trainFct):
    for m in models:
        trainFct(m)
        save(m)


config()
# x, y = load_data("deploy", "train")
# print(models[0].predict(np.array([x,])[0:9]))


########################################################################################################################

