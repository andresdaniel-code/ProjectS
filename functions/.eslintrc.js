module.exports = {
  env: {
    es2021: true,
    node: true,
  },
  extends: [
    "eslint:recommended",
  ],
  parserOptions: {
    ecmaVersion: 12,
    sourceType: "module",
  },
  rules: {
    "max-len": "off",                    // Desactiva límite de longitud
    "@typescript-eslint/no-explicit-any": "off",  // Permite 'any' temporalmente
    "no-unused-vars": "warn",
  },
};
