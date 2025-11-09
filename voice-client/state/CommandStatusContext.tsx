import React, { createContext, useContext, useState, ReactNode, useCallback } from "react";

type CommandStatusContextType = {
  isBusy: boolean;
  startCommand: () => void;
  endCommand: () => void;
};

const CommandStatusContext = createContext<CommandStatusContextType | undefined>(undefined);

export function CommandStatusProvider({ children }: { children: ReactNode }) {
  const [isBusy, setIsBusy] = useState(false);

  const startCommand = useCallback(() => {
    setIsBusy(true);
  }, []);

  const endCommand = useCallback(() => {
    setIsBusy(false);
  }, []);

  return (
    <CommandStatusContext.Provider value={{ isBusy, startCommand, endCommand }}>
      {children}
    </CommandStatusContext.Provider>
  );
}

export function useCommandStatus() {
  const ctx = useContext(CommandStatusContext);
  if (!ctx) {
    throw new Error("useCommandStatus must be used inside CommandStatusProvider");
  }
  return ctx;
}
