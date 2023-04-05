from keras import layers
from tensorflow import keras


def createMLP(shape, activations, name):

    layers_list = []
    for i in range(len(shape) - 1):

        if i == 0:
            layers_list.append(layers.Dense(shape[1], activation=activations[0], input_shape=(shape[0],)))
        else:
            layers_list.append(layers.Dense(shape[i+1], activation=activations[i]))

    model = keras.Sequential(layers_list)
    model._name = name
    # Compile the model
    model.compile(optimizer='adam',
                   loss='categorical_crossentropy',
                   metrics=['accuracy'])

    return model


