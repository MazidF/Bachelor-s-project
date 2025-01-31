import numpy as np
from tensorflow import keras
import librosa


def mfcc(y, sr, n_mfcc=13):
    return np.mean(librosa.feature.mfcc(y=y, n_mfcc=n_mfcc, sr=sr), axis=1)


class DetectorModel2:
    def __init__(self):
        self.__model = keras.models.load_model('models/model2/keras_model_2.keras')

    def __extract_features(self, audio):
        signal = librosa.load(audio)
        features = mfcc(signal[0], signal[1])
        return features.reshape(1, -1)

    def predict(self, file_path):
        features = self.__extract_features(file_path)
        return self.__model.predict(features)
