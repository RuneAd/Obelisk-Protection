Here's the full README content that you can easily copy and paste:

```markdown
# Obelisk Protection Plugin

A RuneLite plugin that helps protect players from accidentally using their POH Wilderness Obelisk when carrying valuable items, potentially preventing costly mistakes and item loss.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/car_role)

## Features

- **Wealth-Based Protection**: Automatically prevents teleport options when carrying items exceeding a configurable value threshold
  - Calculates risk value excluding your 3 most valuable items (similar to item protection mechanics)
  - Default threshold is set to 1M GP
  - Removes teleport-related menu options when protection is active

- **Visual Indicators**: 
  - Displays "Protection Active" text on the ground near the obelisk when protection is engaged
  - Customizable marker color to match your preferences
  - Option to toggle ground markers on/off

## Configuration

The plugin offers several configurable options:

- **Wealth Threshold**: Set the minimum risk value (excluding 3 most valuable items) that will trigger protection
- **Show Ground Marker**: Toggle the visibility of the "Protection Active" ground text
- **Marker Color**: Customize the color of the ground marker text

## How It Works

1. The plugin monitors your inventory and equipment value
2. When interacting with a POH Wilderness Obelisk, it:
   - Calculates your risk value (excluding 3 most valuable items)
   - If the risk exceeds your set threshold:
     - Removes the teleport menu options
     - Displays the ground marker (if enabled)
   - If risk is below threshold:
     - Allows normal obelisk operation

## Protected Menu Options

The following obelisk menu options are removed when protection is active:
- "Teleport to destination"
- "Activate"
- "Set destination"

## Safety Note

While this plugin helps prevent accidental teleports, it's always recommended to:
- Double-check your inventory value before entering the wilderness
- Keep valuable items in your bank when possible
- Be aware that plugin protection can be manually overridden if needed

## Support

For issues, suggestions, or contributions, please visit the plugin's repository on GitHub.

---

Tags: `wilderness`, `obelisk`, `protection`, `poh`
```

You can now copy this entire block and use it as your README.md file. The formatting will be preserved when you paste it into a markdown file.