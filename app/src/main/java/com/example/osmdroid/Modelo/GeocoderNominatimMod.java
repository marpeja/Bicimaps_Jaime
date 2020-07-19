package com.example.osmdroid.Modelo;

import android.location.Address;
import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Locale;

public class GeocoderNominatimMod extends GeocoderNominatim {

    public GeocoderNominatimMod(Locale locale, String userAgent) {
        super(locale, userAgent);
        this.setService("https://nominatim.openstreetmap.org/");
    }

    public GeocoderNominatimMod(String userAgent) {
        super(userAgent);
        this.setService("https://nominatim.openstreetmap.org/");
    }

    @Override
    protected Address buildAndroidAddress(JsonObject jResult) throws JsonSyntaxException {
        super.buildAndroidAddress(jResult);
        Address gAddress = new Address(this.mLocale);
        if (jResult.has("lat") && jResult.has("lon") && jResult.has("address")) {
            gAddress.setLatitude(jResult.get("lat").getAsDouble());
            gAddress.setLongitude(jResult.get("lon").getAsDouble());
            JsonObject jAddress = jResult.get("address").getAsJsonObject();
            int addressIndex = 0;
            if (jAddress.has("road")) {
                gAddress.setAddressLine(addressIndex++, jAddress.get("road").getAsString());
                gAddress.setThoroughfare(jAddress.get("road").getAsString());
            }
            if(jAddress.has("house_number")){
                gAddress.setPhone(jAddress.get("house_number").getAsString());
            }

            if (jAddress.has("suburb")) {
                gAddress.setSubLocality(jAddress.get("suburb").getAsString());
            }

            if (jAddress.has("postcode")) {
                gAddress.setAddressLine(addressIndex++, jAddress.get("postcode").getAsString());
                gAddress.setPostalCode(jAddress.get("postcode").getAsString());
            }

            if (jAddress.has("city")) {
                gAddress.setAddressLine(addressIndex++, jAddress.get("city").getAsString());
                gAddress.setLocality(jAddress.get("city").getAsString());
            } else if (jAddress.has("town")) {
                gAddress.setAddressLine(addressIndex++, jAddress.get("town").getAsString());
                gAddress.setLocality(jAddress.get("town").getAsString());
            } else if (jAddress.has("village")) {
                gAddress.setAddressLine(addressIndex++, jAddress.get("village").getAsString());
                gAddress.setLocality(jAddress.get("village").getAsString());
            }

            if (jAddress.has("county")) {
                gAddress.setSubAdminArea(jAddress.get("county").getAsString());
            }

            if (jAddress.has("state")) {
                gAddress.setAdminArea(jAddress.get("state").getAsString());
            }

            if (jAddress.has("country")) {
                gAddress.setAddressLine(addressIndex++, jAddress.get("country").getAsString());
                gAddress.setCountryName(jAddress.get("country").getAsString());
            }

            if (jAddress.has("country_code")) {
                gAddress.setCountryCode(jAddress.get("country_code").getAsString());
            }

            Bundle extras = new Bundle();
            JsonArray jPolygonPoints;
            if (jResult.has("polygonpoints")) {
                jPolygonPoints = jResult.get("polygonpoints").getAsJsonArray();
                ArrayList<GeoPoint> polygonPoints = new ArrayList(jPolygonPoints.size());

                for(int i = 0; i < jPolygonPoints.size(); ++i) {
                    JsonArray jCoords = jPolygonPoints.get(i).getAsJsonArray();
                    double lon = jCoords.get(0).getAsDouble();
                    double lat = jCoords.get(1).getAsDouble();
                    GeoPoint p = new GeoPoint(lat, lon);
                    polygonPoints.add(p);
                }

                extras.putParcelableArrayList("polygonpoints", polygonPoints);
            }

            if (jResult.has("boundingbox")) {
                jPolygonPoints = jResult.get("boundingbox").getAsJsonArray();
                BoundingBox bb = new BoundingBox(jPolygonPoints.get(1).getAsDouble(), jPolygonPoints.get(2).getAsDouble(), jPolygonPoints.get(0).getAsDouble(), jPolygonPoints.get(3).getAsDouble());
                extras.putParcelable("boundingbox", bb);
            }

            if (jResult.has("osm_id")) {
                long osm_id = jResult.get("osm_id").getAsLong();
                extras.putLong("osm_id", osm_id);
            }

            String display_name;
            if (jResult.has("osm_type")) {
                display_name = jResult.get("osm_type").getAsString();
                extras.putString("osm_type", display_name);
            }

            if (jResult.has("display_name")) {
                display_name = jResult.get("display_name").getAsString();
                extras.putString("display_name", display_name);
            }

            gAddress.setExtras(extras);
            return gAddress;
        } else {
            return null;
        }
    }
}
