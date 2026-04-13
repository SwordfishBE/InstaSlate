package net.instaslate.config;

public class InstaSlateConfig {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void validate() {
        // Reserved for future config options.
    }

    @Override
    public String toString() {
        return "InstaSlateConfig{enabled=" + enabled + "}";
    }
}
