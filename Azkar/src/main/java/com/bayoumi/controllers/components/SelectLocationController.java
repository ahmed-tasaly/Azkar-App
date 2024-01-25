package com.bayoumi.controllers.components;

import com.bayoumi.models.location.City;
import com.bayoumi.models.location.Country;
import com.bayoumi.models.settings.Language;
import com.bayoumi.models.settings.LanguageBundle;
import com.bayoumi.models.settings.PrayerTimeSettings;
import com.bayoumi.models.settings.Settings;
import com.bayoumi.util.Logger;
import com.bayoumi.util.Utility;
import com.bayoumi.util.gui.ComboBoxAutoComplete;
import com.bayoumi.util.web.IpChecker;
import com.bayoumi.util.web.LocationService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class SelectLocationController implements Initializable {
    private PrayerTimeSettings prayerTimeSettings;
    private ResourceBundle bundle;
    @FXML
    private Label enterCountryAndCity, statusLabel;
    @FXML
    public JFXComboBox<Country> countries;
    @FXML
    public JFXComboBox<City> cities;
    @FXML
    public JFXTextField manualLongitude, manualLatitude, autoCountry, autoCity, autoLongitude, autoLatitude;
    @FXML
    public JFXButton autoLocationButton, autoSelectButton, manualSelectButton;
    public JFXButton selectedButton;
    private ComboBoxAutoComplete<City> cityComboBoxAutoComplete;
    private ComboBoxAutoComplete<Country> countryComboBoxAutoComplete;
    @FXML
    private VBox manualContainer, autoContainer;

    public void setData() {
        updateBundle(LanguageBundle.getInstance().getResourceBundle());
        // init selected button
        toggleSelectButtonStyle(manualSelectButton);
    }

    private void updateBundle(ResourceBundle bundle) {
        this.bundle = bundle;
        countries.setPromptText(Utility.toUTF(bundle.getString("country")));
        cities.setPromptText(Utility.toUTF(bundle.getString("city")));
        manualLongitude.setPromptText(Utility.toUTF(bundle.getString("longitude")));
        manualLatitude.setPromptText(Utility.toUTF(bundle.getString("latitude")));
        autoCountry.setPromptText(Utility.toUTF(bundle.getString("country")));
        autoCity.setPromptText(Utility.toUTF(bundle.getString("city")));
        autoLongitude.setPromptText(Utility.toUTF(bundle.getString("longitude")));
        autoLatitude.setPromptText(Utility.toUTF(bundle.getString("latitude")));
        autoLocationButton.setText(Utility.toUTF(bundle.getString("autoLocate")));
        enterCountryAndCity.setText(Utility.toUTF(bundle.getString("enterCountryAndCity")));
        initCountries();
        initCities();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LanguageBundle.getInstance().addObserver((o, arg) -> updateBundle(LanguageBundle.getInstance().getResourceBundle()));
        prayerTimeSettings = Settings.getInstance().getPrayerTimeSettings();
        statusLabel.setVisible(false);
        countryComboBoxAutoComplete = new ComboBoxAutoComplete<>(countries);
        cityComboBoxAutoComplete = new ComboBoxAutoComplete<>(cities);
        initDoubleValidation();
    }

    private void initDoubleValidation() {
        Pattern validEditingState = Pattern.compile("-?(([1-9][0-9]*)|0)?(\\.[0-9]*)?");
        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c;
            } else {
                return null;
            }
        };

        StringConverter<Double> converter = new StringConverter<Double>() {

            @Override
            public Double fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0.0;
                } else {
                    return Double.valueOf(s);
                }
            }

            @Override
            public String toString(Double d) {
                return d.toString();
            }
        };
        manualLatitude.setTextFormatter(new TextFormatter<>(converter, 0.0, filter));
        manualLongitude.setTextFormatter(new TextFormatter<>(converter, 0.0, filter));
    }

    private void initCountries() {
        // getAllData
        final String local = Language.getLocalFromPreferences();
        countries.getItems().addAll(Country.getAll(local));
        countryComboBoxAutoComplete.setItems(countries.getItems());
        // StringConverter
        countries.setConverter(new StringConverter<Country>() {
            @Override
            public String toString(Country object) {
                if (local.equals("ar")) {
                    return object != null ? object.getArabicName() : "";
                }
                return object != null ? object.getEnglishName() : "";
            }

            @Override
            public Country fromString(String string) {
                if (local.equals("ar")) {
                    return countries.getItems().stream().filter(object ->
                            object.getArabicName().equals(string)).findFirst().orElse(null);
                }
                return countries.getItems().stream().filter(object ->
                        object.getEnglishName().equals(string)).findFirst().orElse(null);
            }
        });
        countries.setOnAction(this::changeCountry);
        if (!countries.getItems().isEmpty()) {
            countries.setValue(countries.getItems().get(0));
        }
        Country countryFormCode = Country.getCountryFormCode(prayerTimeSettings.getCountry());
        if (countryFormCode != null) {
            countries.setValue(countryFormCode);
        }
    }

    private void changeCountry(ActionEvent event) {
        cities.getItems().clear();
        if (countries.getValue() != null) {
            cities.getItems().addAll(City.getCitiesInCountry(countries.getValue().getCode()));
            cityComboBoxAutoComplete.setItems(cities.getItems());
        }
        if (!cities.getItems().isEmpty()) {
            cities.setValue(cities.getItems().get(0));
        }
        statusLabel.setVisible(false);
    }

    private void initCities() {
        cities.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue != null) {
                manualLatitude.setText(String.valueOf(cities.getValue().getLatitude()));
                manualLongitude.setText(String.valueOf(cities.getValue().getLongitude()));
                statusLabel.setVisible(false);
            }
        });
        if (!countries.getItems().isEmpty()) {
            cities.getItems().clear();
            if (countries.getValue() != null) {
                cities.getItems().addAll(City.getCitiesInCountry(countries.getValue().getCode()));
                cityComboBoxAutoComplete.setItems(cities.getItems());
            }
            if (!cities.getItems().isEmpty()) {
                cities.setValue(cities.getItems().get(0));
            }
        }
        final String local = Language.getLocalFromPreferences();
        cities.setConverter(new StringConverter<City>() {
            @Override
            public String toString(City object) {
                if (local.equals("ar")) {
                    if (object.getArabicName() == null || object.getArabicName().trim().isEmpty()) {
                        return object.getEnglishName();
                    }
                    return object.getArabicName();
                }
                return object.getEnglishName();
            }

            @Override
            public City fromString(String string) {
                return null;
            }
        });
        City cityFromEngName = City.getCityFromEngName(prayerTimeSettings.getCity(), prayerTimeSettings.getCountry());
        if (cityFromEngName != null) {
            cities.setValue(cityFromEngName);
            cities.getSelectionModel().select(cityFromEngName);
        }
    }

    @FXML
    private void getAutoLocation() {
        countries.setDisable(true);
        cities.setDisable(true);
        manualLatitude.setDisable(true);
        manualLongitude.setDisable(true);
        autoLocationButton.setDisable(true);
        statusLabel.setVisible(true);
        statusLabel.setText(Utility.toUTF(bundle.getString("loading")) + "..");
        statusLabel.setStyle("-fx-text-fill: green");
        new Thread(() -> {
            try {
                City city = LocationService.getCity(IpChecker.getIp());
                Logger.info("LocationService.getCity(IP): " + city);
                Country countryFormCode = Country.getCountryFormCode(city.getCountryCode());
                if (countryFormCode != null) {
                    Platform.runLater(() -> countries.setValue(countryFormCode));
                } else {
                    throw new Exception("Error in fetching city => cannot getCountryFormCode()!");
                }
                City cityFromEngName = City.getCityFromEngName(city.getEnglishName(), city.getCountryCode());
                if (cityFromEngName != null) {
                    Platform.runLater(() -> {
                        cities.setValue(cityFromEngName);
                        cities.getSelectionModel().select(cityFromEngName);
                        manualLatitude.setText(String.valueOf(cityFromEngName.getLatitude()));
                        manualLatitude.setText(String.valueOf(cityFromEngName.getLongitude()));
                    });
                } else {
                    City cityFromCoordinates = City.getCityFromCoordinates(city.getLongitude(), city.getLatitude(), city.getCountryCode());
                    if (cityFromCoordinates != null) {
                        Platform.runLater(() -> {
                            cities.setValue(cityFromCoordinates);
                            cities.getSelectionModel().select(cityFromCoordinates);
                            manualLatitude.setText(String.valueOf(cityFromCoordinates.getLatitude()));
                            manualLatitude.setText(String.valueOf(cityFromCoordinates.getLongitude()));
                        });
                    } else {
                        throw new Exception("Error in getCityFromCoordinates() && getCityFromEngName(): " + city);
                    }
                }
                statusLabel.setVisible(false);
            } catch (Exception ex) {
                Logger.error(null, ex, getClass().getName() + ".getAutoLocation()");
                Platform.runLater(() -> {
                    statusLabel.setVisible(true);
                    statusLabel.setText(Utility.toUTF(bundle.getString("autoSelectionError")));
                    statusLabel.setStyle("-fx-text-fill: red");
                });
            }
            countries.setDisable(false);
            cities.setDisable(false);
            manualLatitude.setDisable(false);
            manualLongitude.setDisable(false);
            autoLocationButton.setDisable(false);
        }).start();
    }

    @FXML
    private void manualSelect() {
        if (selectedButton != null && selectedButton.equals(manualSelectButton)) {
            return;
        }
        toggleSelectButtonStyle(manualSelectButton);
        manualContainer.setVisible(true);
        autoContainer.setVisible(false);
    }

    @FXML
    private void autoSelect() {
        if (selectedButton != null && selectedButton.equals(autoSelectButton)) {
            return;
        }
        toggleSelectButtonStyle(autoSelectButton);
        manualContainer.setVisible(false);
        autoContainer.setVisible(true);
    }

    private void toggleSelectButtonStyle(JFXButton newButton) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("secondary-button");
        }
        selectedButton = newButton;
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("secondary-button");
        }
    }
}
