package com.philipoy.app.quickcontact.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class QuickContact implements Parcelable {

	public String contactId;
	public String contactEOL;
	public List<QuickContactData> contactData;
	
	public static final Parcelable.Creator<QuickContact> CREATOR
     = new Parcelable.Creator<QuickContact>() {
		@Override
		public QuickContact createFromParcel(Parcel source) {
            // contact with id and eol parameters
            QuickContact contact = new QuickContact(source.readString(), source.readString());
            // read couple <name, phone> and add them to the list of contact's data
            while (source.dataAvail() > 0) {
                contact.addContactData(source.readString(), source.readString());
            }
			return contact;
		}
		@Override
		public QuickContact[] newArray(int size) {
			return new QuickContact[size];
		}
	};
	
	public QuickContact(String id, String eol) {
		contactId = id;
		contactEOL = eol;
		contactData = new ArrayList<>();
	}

	public void addContactData(String name, String phone) {
		if (contactData != null) {
			QuickContactData data = new QuickContactData();
			data.contactName = name;
			data.contactPhone = phone;
			contactData.add(data);
		}
	}

    public String getContactPrimaryName() {
        if (contactData != null && contactData.size() > 0)
            return contactData.get(0).contactName;
        else
            return "";
    }

    public String getContactPrimaryPhone() {
        if (contactData != null && contactData.size() > 0)
            return contactData.get(0).contactPhone;
        else
            return "";
    }
	
	public String toString() {
        if (contactData == null || contactData.size() == 0) {
			// TODO i18n
            return "Contact information deleted";
        } else {
            int dataCount = contactData.size();
            QuickContactData data = contactData.get(0);
            if (dataCount == 1) {
                return String.format("%s (%s)", data.contactName, data.contactPhone);
            } else {
                return String.format("%s (%s + %d more)", data.contactName, data.contactPhone, (dataCount-1));
            }
        }
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(contactId);
		dest.writeString(contactEOL);
		if (this.contactData != null) {
            // write the contact's data as successive strings
            for (QuickContactData data: this.contactData) {
                dest.writeString(data.contactName);
                dest.writeString(data.contactPhone);
            }
		}
	}
	
	
	/**
	 * Class that contains real data about a contact:<br/>
	 * - his name
	 * - his phone number
	 * @author philippeexo
	 *
	 */
	public class QuickContactData {
		public String contactName;
		public String contactPhone;
	}
}
