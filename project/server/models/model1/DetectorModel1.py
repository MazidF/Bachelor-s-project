import joblib
from disvoice.phonation import Phonation
from pandas import DataFrame
import numpy as np
import pandas as pd
from scipy.signal import savgol_filter


class DetectorModel1:
    def __init__(self):
        self.__phonation = Phonation()
        self.__model = joblib.load("models/model1/pathology_model.pkl")

    def __extract_features(self, audio) -> DataFrame:
        return self.__phonation.extract_features_file(
            audio=audio,
            static=True,
            plots=False,
            fmt="dataframe",
        )

    def __convert_disvoice_feature_to_model_features(self, extracted_features, age):
        # Compute derivatives (example using Savitzky-Golay filter for smooth derivatives)
        data_series = extracted_features['avg DF0'].values.flatten()  # Example series
        first_derivative = savgol_filter(data_series, window_length=3, polyorder=1, deriv=1, mode='nearest')
        second_derivative = savgol_filter(data_series, window_length=3, polyorder=1, deriv=2, mode='nearest')
        # Placeholder for MFCCs (assume precomputed or use an external library like librosa)
        mfcc_features = np.random.random(13)  # Replace with actual MFCC computation
        mfcc_1 = mfcc_features[0]
        mfcc_3 = mfcc_features[2]
        mfcc_8 = mfcc_features[7]
        # Combine the features into the required format
        return pd.DataFrame({
            'age': [age],
            '1st-derivative': [np.mean(first_derivative)],  # Example: using mean
            'MFCC-3': [mfcc_3],
            'MFCC-1': [mfcc_1],
            'MFCC-8': [mfcc_8],
            '2nd-derivative': [np.mean(second_derivative)]  # Example: using mean
        })

    def predict(self, file_path, age):
        features = self.__extract_features(file_path)
        converted_feature = self.__convert_disvoice_feature_to_model_features(features, age)
        return self.__model.predict(converted_feature)

