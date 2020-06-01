package org.kilocraft.essentials.util;

import com.google.gson.JsonObject;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class MuteEntry<T> extends ServerConfigEntry<T> {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    protected final Date creationDate;
    protected final String source;
    protected final Date expiryDate;
    protected final String reason;

    public MuteEntry(T object, @Nullable Date creationDate, @Nullable String source, @Nullable Date expiryDate, @Nullable String reason) {
        super(object);
        this.creationDate = creationDate;
        this.source = source;
        this.expiryDate = expiryDate;
        this.reason = reason;
    }

    protected MuteEntry(T object, JsonObject jsonObject) {
        super(object);
        Date created;
        try {
            created = jsonObject.has("created") ? DATE_FORMAT.parse(jsonObject.get("created").getAsString()) : new Date();
        } catch (ParseException e) {
            created = new Date();
        }

        Date expiry;
        try {
            expiry = jsonObject.has("expires") ? DATE_FORMAT.parse(jsonObject.get("expires").getAsString()) : new Date();
        } catch (ParseException e) {
            expiry = new Date();
        }

        this.expiryDate = expiry;
        this.creationDate = created;
        this.source = jsonObject.has("source") ? jsonObject.get("source").getAsString() : "(Unknown)";
        this.reason = jsonObject.has("reason") ? jsonObject.get("reason").getAsString() : null;
    }

    public String getSource() {
        return this.source;
    }

    public Date getExpiryDate() {
        return this.expiryDate;
    }

    public String getReason() {
        return this.reason;
    }

    public abstract Text toText();

    boolean isInvalid() {
        return this.expiryDate != null && this.expiryDate.before(new Date());
    }

    @Override
    protected void fromJson(JsonObject jsonObject) {
        jsonObject.addProperty("created", DATE_FORMAT.format(this.creationDate));
        jsonObject.addProperty("source", this.source);
        jsonObject.addProperty("expires", this.expiryDate == null ? "forever" : DATE_FORMAT.format(this.expiryDate));
        jsonObject.addProperty("reason", this.reason);
    }
}
