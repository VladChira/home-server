"use client"

import { MetricsChartCard } from "./MetricsChartCard";
import { useCPUUsage, useRAMUsage, useTempUsage } from "@/lib/metrics";


export default function DashboardCard() {

  const sinceSeconds = 60 * 60 * 12;   // 12h
  const stepSeconds = 60 * 15;         // 15m

  const { data: ramData = [], isLoading: isRAMLoading, error: ramError } = useRAMUsage(sinceSeconds, stepSeconds);
  const { data: cpuData = [], isLoading: isCPULoading, error: cpuError } = useCPUUsage(sinceSeconds, stepSeconds);
  const { data: tempData = [], isLoading: isTempLoading, error: tempError } = useTempUsage(sinceSeconds, stepSeconds);

  return (
    <div className="flex flex-1 flex-col">
      <div className="@container/main flex flex-1 flex-col gap-2">
        <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">

          <div className="flex gap-4">
            <div className="w-1/3"><MetricsChartCard data={ramData} title="Server RAM usage" description="RAM usage over the past 12 hours" /></div>
            <div className="w-1/3"><MetricsChartCard data={cpuData} title="Server CPU usage" description="CPU usage over the past 12 hours" /></div>
            <div className="w-1/3"><MetricsChartCard data={tempData} title="Server CPU temperature" description="CPU temperature (C°) over the past 12 hours" /></div>
          </div>
        </div>
      </div>
    </div>
  );
}
