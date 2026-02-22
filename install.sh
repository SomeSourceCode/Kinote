#!/bin/bash

INSTALL_DIR="$HOME/.kinote/bin"
WRAPPER_DIR="$HOME/.local/bin"
JAR_URL="https://github.com/SomeSourceCode/kinote/releases/latest/download/kinote.jar"

echo "Downloading Kinote..."
mkdir -p "$INSTALL_DIR"
mkdir -p "$WRAPPER_DIR"

# download latest jar
curl -sSL "$JAR_URL" -o "$INSTALL_DIR/kinote.jar"

echo "Creating executable wrapper..."
# write wrapper script
cat << 'EOF' > "$WRAPPER_DIR/kinote"
#!/bin/bash
java -jar "$HOME/.kinote/bin/kinote.jar" "$@"
EOF

# make wrapper executable that forwards all arguments ($@)
chmod +x "$WRAPPER_DIR/kinote"

echo "Kinote installed successfully!"

# Check if WRAPPER_DIR is in the current PATH
if [[ ":$PATH:" != *":$WRAPPER_DIR:"* ]]; then
  echo ""
  echo "==========================================================="
  echo "  WARNING: $WRAPPER_DIR is not in your PATH."
  echo "  You must add it to run 'kinote' globally."
  echo ""
  echo "  For Zsh, run:"
  echo "  echo 'export PATH=\"$WRAPPER_DIR:\$PATH\"' >> ~/.zshrc"
  echo ""
  echo "  For Bash, run:"
  echo "  echo 'export PATH=\"$WRAPPER_DIR:\$PATH\"' >> ~/.bashrc"
  echo ""
  echo "  Then restart your terminal or run 'source ~/.zshrc' or 'source ~/.bashrc' to apply the changes."
  echo "==========================================================="
fi
