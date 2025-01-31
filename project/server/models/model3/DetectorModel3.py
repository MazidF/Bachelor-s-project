from tensorflow import keras
import numpy as np
import librosa

Z = np.array([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0])
for i in range(20):
    Z[i] = np.float64(Z[i])


class DetectorModel3:
    def __init__(self):
        self.__model = keras.models.load_model('models/model3/model_test_MFCC.keras')

    def __extract_features(self, audio):
        y1, sr1 = librosa.load(audio, sr=None, mono=True)
        features = librosa.feature.mfcc(y=y1, sr=sr1, n_mfcc=20)
        features = [i.T[0:2095] for i in features]

        for i in range(len(features)):
            if 2095 != len(features[i]):
                features[i] = np.append(features[i], np.zeros(2095 - len(features[i])))

        return np.array(features)

    def predict(self, file_path):
        features = self.__extract_features(file_path)
        features = np.expand_dims(features.T, axis=0)
        features = np.expand_dims(features, axis=-1)
        return self.__model.predict(features)
