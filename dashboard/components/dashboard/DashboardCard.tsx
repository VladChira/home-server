"use server"

import { MemoryUsageCard } from "./MemoryUsageCard";
import { fetchCPUUsage, fetchMemoryUsage, fetchTemperature } from "@/app/actions/dashboardServerActions";
import { CPUUsageCard } from "./CPUUsageCard";
import { SectionCards } from "./SectionCards";
import { TempCard } from "./TempCard";


export default async function DashboardCard() {
    var ramData = await fetchMemoryUsage();
    var cpuData = await fetchCPUUsage();
    var tempData = await fetchTemperature();
   
    return (
        <div className="flex flex-1 flex-col">
          <div className="@container/main flex flex-1 flex-col gap-2">
            <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
                <SectionCards />

                <div className="flex gap-4">
                    <div className="w-1/3"><MemoryUsageCard data={ramData} /></div>
                    <div className="w-1/3"><CPUUsageCard data={cpuData} /></div>
                    <div className="w-1/3"><TempCard data={tempData} /></div>
                </div>
            </div>
          </div>
        </div>
    );
}
