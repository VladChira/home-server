import { CommandStatusProvider } from "@/state/CommandStatusContext";
import React from "react";
import MainScreen from "./MainScreen";

export default function App() {
  return (
    <CommandStatusProvider>
      <MainScreen />
    </CommandStatusProvider>
  );
}
