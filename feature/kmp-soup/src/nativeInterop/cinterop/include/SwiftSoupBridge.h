#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class KmpSoupBridgeDocument;
@class KmpSoupBridgeElement;
@class KmpSoupBridgeElements;

@interface KmpSoupBridgeParser : NSObject
+ (nullable KmpSoupBridgeDocument

*)parseHtml:(NSString *)
html;
@end

@interface KmpSoupBridgeDocument : NSObject
- (KmpSoupBridgeElements *)select:(NSString *)css;

- (nullable KmpSoupBridgeElement

*)selectFirst:(NSString *)
css;

- (nullable KmpSoupBridgeElement

*)getElementById:(NSString *)
elementId;

- (KmpSoupBridgeElements *)getElementsByTag:(NSString *)tag;

- (KmpSoupBridgeElements *)getElementsByClass:(NSString *)className;

- (NSString *)title;

- (void)setTitle:(NSString *)title;

- (nullable KmpSoupBridgeElement

*)
body;

- (void)setText:(NSString *)text;

- (NSString *)html;

- (NSString *)outerHtml;
@end

@interface KmpSoupBridgeElement : NSObject
- (KmpSoupBridgeElements *)select:(NSString *)css;

- (nullable KmpSoupBridgeElement

*)selectFirst:(NSString *)
css;

- (nullable KmpSoupBridgeElement

*)getElementById:(NSString *)
elementId;

- (KmpSoupBridgeElements *)getElementsByTag:(NSString *)tag;

- (KmpSoupBridgeElements *)getElementsByClass:(NSString *)className;

- (NSString *)text;

- (NSString *)ownText;

- (NSString *)attr:(NSString *)name;

- (BOOL)hasAttr:(NSString *)name;

- (void)setAttrName:(NSString *)name value:(NSString *)value;

- (void)removeAttrName:(NSString *)name;

- (NSString *)id;

- (NSString *)tagName;

- (NSString *)className;

- (BOOL)hasClassName:(NSString *)className;

- (void)addClassName:(NSString *)className;

- (void)removeClassName:(NSString *)className;

- (void)toggleClassName:(NSString *)className;

- (NSString *)value;

- (void)setValue:(NSString *)value;

- (nullable KmpSoupBridgeElement

*)
parent;

- (KmpSoupBridgeElements *)children;

- (nullable KmpSoupBridgeElement

*)childAt:(NSInteger)
index;

- (NSInteger)childSize;

- (KmpSoupBridgeElements *)parents;

- (KmpSoupBridgeElements *)siblingElements;

- (KmpSoupBridgeElements *)nextElementSiblings;

- (KmpSoupBridgeElements *)previousElementSiblings;

- (nullable KmpSoupBridgeElement

*)
firstElementSibling;

- (nullable KmpSoupBridgeElement

*)
lastElementSibling;

- (NSInteger)elementSiblingIndex;

- (BOOL)hasTextContent;

- (BOOL)matchesCss:(NSString *)cssQuery;

- (nullable KmpSoupBridgeElement

*)closest:(NSString *)
cssQuery;

- (void)setTagName:(NSString *)tagName;

- (void)setHtml:(NSString *)html;

- (void)prependHtml:(NSString *)html;

- (void)appendHtml:(NSString *)html;

- (void)beforeHtml:(NSString *)html;

- (void)afterHtml:(NSString *)html;

- (void)wrapHtml:(NSString *)html;

- (void)unwrapNode;

- (void)emptyNode;

- (void)removeNode;

- (NSString *)html;

- (NSString *)outerHtml;
@end

@interface KmpSoupBridgeElements : NSObject
- (NSInteger)size;

- (BOOL)isEmpty;

- (nullable KmpSoupBridgeElement

*)
firstOrNull;

- (nullable KmpSoupBridgeElement

*)
lastOrNull;

- (nullable KmpSoupBridgeElement

*)elementAt:(NSInteger)
index;

- (NSString *)text;

- (NSString *)attr:(NSString *)name;
@end

NS_ASSUME_NONNULL_END
