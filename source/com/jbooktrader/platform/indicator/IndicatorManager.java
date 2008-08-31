package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;

import java.util.*;

/**
 *
 */
public class IndicatorManager {

    private MarketBook marketBook;
    private final List<ChartableIndicator> indicators;
    private final boolean isOptimizationMode;
    private boolean hasValidIndicators;

    public IndicatorManager(MarketBook marketBook) {
        this.marketBook = marketBook;
        indicators = new LinkedList<ChartableIndicator>();
        isOptimizationMode = (Dispatcher.getMode() == Optimization);
    }

    public void setMarketBook(MarketBook marketBook) {
        for (ChartableIndicator chartableIndicator : indicators) {
            chartableIndicator.getIndicator().setMarketBook(marketBook);
        }
    }

    public boolean hasValidIndicators() {
        return hasValidIndicators;
    }

    public void addIndicator(Indicator indicator) {
        indicator.setMarketBook(marketBook);
        ChartableIndicator chartableIndicator = new ChartableIndicator(indicator);
        indicators.add(chartableIndicator);
    }

    public List<ChartableIndicator> getIndicators() {
        return indicators;
    }

    public void updateIndicators() {
        hasValidIndicators = true;
        long time = marketBook.getLastMarketSnapshot().getTime();
        for (ChartableIndicator chartableIndicator : indicators) {
            Indicator indicator = chartableIndicator.getIndicator();
            try {
                indicator.calculate();
                if (!isOptimizationMode) {
                    chartableIndicator.add(time, indicator.getValue());
                }
            } catch (IndexOutOfBoundsException iob) {
                hasValidIndicators = false;
                // This exception will occur if book size is insufficient to calculate
                // the indicator. This is normal.
            } catch (Exception e) {
                throw new JBookTraderException(e);
            }
        }
    }
}
