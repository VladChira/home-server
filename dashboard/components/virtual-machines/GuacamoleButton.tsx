import { useState } from "react";
import { Button } from "@/components/ui/button";
import { ExternalLink } from "lucide-react";
import { getGuacToken, VirtualMachine } from "@/lib/vm";

type Props = {
    vm: VirtualMachine;
};

const PUBLIC_DOMAIN = process.env.NEXT_PUBLIC_DOMAIN;

export function GuacamoleButton({ vm }: Props) {
    const [loading, setLoading] = useState(false);

    const handleOpenGuac = async () => {
        if (!vm.guac_id) return;

        try {
            setLoading(true);
            const token = await getGuacToken();

            const url = `${PUBLIC_DOMAIN}/guacamole/#/client/${vm.guac_id}?token=${encodeURIComponent(
                token
            )}`;

            // open in new tab, no opener reference
            window.open(url, "_blank", "noopener,noreferrer");
        } catch (err) {
            console.error("Failed to open Guacamole", err);
        } finally {
            setLoading(false);
        }
    };

    if (!vm.guac_id) {
        return (
            <div className="flex items-center gap-2">
                <span className="text-small text-muted-foreground">N/A</span>
            </div>
        );
    }

    return (
        <div className="flex items-center gap-2">
            <Button
                className="bg-accent-foreground"
                onClick={handleOpenGuac}
                disabled={loading || !vm.guac_id}
            >
                <ExternalLink className="mr-1 h-4 w-4" />
                {loading ? "Opening..." : "Open"}
            </Button>
        </div>
    );
}
