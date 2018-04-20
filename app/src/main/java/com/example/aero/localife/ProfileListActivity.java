package com.example.aero.localife;

public class ProfileListActivity {

 private String profileName;
 private String latitudeValue;
 private String longitudeValue;
 private String bluetoothStatus;
 public ProfileListActivity(String profileName) {
  this.profileName = profileName;
 }

 public String getName() {
  return profileName;
 }
 //setter method
 public void setName(String profileName) {
  this.profileName = profileName;
 }

 public ProfileListActivity(){} // empty constructor

 public ProfileListActivity(String profileName, String latitudeValue, String longitudeValue, String bluetoothStatus) {
   this.profileName = profileName;
   this.latitudeValue = latitudeValue;
   this.longitudeValue = longitudeValue;
   this.bluetoothStatus = bluetoothStatus;
 }

 //setter method
 public void setProfileName(String profileName) {
  this.profileName = profileName;
 }

 //getter method
 public String getProfileName() {
  return profileName;
 }

 //setter method
 public void setLatitudeValue(String latitudeValue) {
  this.latitudeValue = latitudeValue;
 }

 //getter method
 public String getLatitudeValue() {
  return latitudeValue;
 }

 //setter method
 public void setLongitudeValue(String longitudeValue){
        this.longitudeValue = longitudeValue;
    }

 //getter method
 public String getLongitudeValue(){
        return longitudeValue;
    }

 //setter method
 public void setBluetoothStatus(String bluetoothStatus){
  this.bluetoothStatus = bluetoothStatus;
 }

 //getter method
 public String getBluetoothStatus(){
  return bluetoothStatus;
 }

}
