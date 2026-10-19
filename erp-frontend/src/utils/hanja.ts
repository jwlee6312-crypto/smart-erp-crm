/**
 * 💡 [Hanja Conversion Utility]
 * Converts numbers to traditional Korean Hanja for financial documents.
 * Based on ASP original logic.
 */
export const numberToHanja = (num: number | string): string => {
    const val = Math.abs(Math.floor(Number(num)));
    if (isNaN(val)) return "";

    const hanjaArr = ["", "壹", "貳", "參", "四", "五", "六", "七", "八", "九", "拾"];
    const unitArr = ["", "拾", "百", "阡", "萬", "拾", "百", "阡", "萬", "億", "拾", "百", "阡", "兆", "拾", "百", "阡"];

    const str = String(val);
    const len = str.length;
    let result = "";

    for (let i = 0; i < len; i++) {
        const digit = parseInt(str[len - 1 - i]);
        let char = "";

        if (digit > 0) {
            char = hanjaArr[digit] + unitArr[i];
        } else {
            // Logic for units like 萬, 億, 兆
            if (i === 4 || i === 8 || i === 12 || i === 15) {
                char = unitArr[i];
            }
        }
        result = char + result;
    }

    return result;
}
