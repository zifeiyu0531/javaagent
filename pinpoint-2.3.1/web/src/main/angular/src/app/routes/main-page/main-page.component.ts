import { Component, OnInit, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, OnDestroy } from '@angular/core';
import { Observable, fromEvent, Subject } from 'rxjs';
import { map, startWith, pluck, takeUntil, delay, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    UrlRouteManagerService
} from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { UrlPathId } from 'app/shared/models';
import { TransactionIdSearchContainerComponent } from 'app/core/components/transaction-id-search/transaction-id-search-container.component';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';

const enum ScreenWidth {
    MIN = 1380
}

@Component({
    selector: 'pp-main-page',
    templateUrl: './main-page.component.html',
    styleUrls: ['./main-page.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainPageComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private guideText: string;

    isAppSelected$: Observable<boolean>;
    isScreenWideEnough$: Observable<boolean>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private translateService: TranslateService,
        private urlRouteManagerService: UrlRouteManagerService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.translateService.get('MAIN.VISIBILITY_HIDDEN').subscribe((text: string) => {
            this.guideText = text;
        });
        this.isAppSelected$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        );
        this.webAppSettingDataService.getVersion().subscribe((version: string) => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.VERSION, version);
        });

        this.addEventListener();
    }

    private addEventListener(): void {
        this.isScreenWideEnough$ = fromEvent(window, 'resize').pipe(
            takeUntil(this.unsubscribe),
            pluck('target', 'innerWidth'),
            startWith(window.innerWidth),
            map((width: number) => width >= ScreenWidth.MIN)
        );

        fromEvent(document, 'visibilitychange').pipe(
            takeUntil(this.unsubscribe),
            filter(() => document.hidden),
            filter(() => this.newUrlStateNotificationService.isRealTimeMode()),
            delay(10000),
            filter(() => document.hidden),
        ).subscribe(() => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Notice',
                    contents: this.guideText,
                    type: 'html'
                },
                component: MessagePopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.reload();
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.NAVBAR);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.NAVBAR,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }

    onClickSearchIcon(btn: HTMLElement): void {
        const {left, top, width, height} = btn.getBoundingClientRect();
        const coord = {
            coordX: left + width / 2,
            coordY: top + height / 2
        };

        this.dynamicPopupService.openPopup({
            coord,
            component: TransactionIdSearchContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
