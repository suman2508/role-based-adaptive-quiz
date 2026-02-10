/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#eef7ff",
          100: "#d9ecff",
          200: "#b9dbff",
          300: "#8ec3ff",
          400: "#5aa3ff",
          500: "#2a7dff",
          600: "#1d5fe6",
          700: "#184dc0",
          800: "#183f97",
          900: "#173a7a"
        }
      }
    }
  },
  plugins: []
};
